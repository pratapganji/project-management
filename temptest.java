import argparse
import datetime
import json
import csv
import sys
import os
import re
import cx_Oracle
import Logger
import subprocess
import time
import jaydebeapi

import sql_parser
import emailer as email


def send_mail(emailer, file=None):
    mail_config = {
        "attach": [],
        "from": emailer.get("from"),
        "to": emailer.get("to"),
    }

    error = emailer["error"]
    message = emailer["message"]
    report_date = emailer["report_date"]
    env = emailer["env"]

    source_map = {
        "rest": "REST_API",
        "jdbc": "JDBC_API",
        "bulk": "BULK_API",
    }
    source = source_map[emailer["source"]]

    if error is True:
        mail_config[
            "subject"
        ] = (
            f"FAILURE: Error Occured in User Consumption Feed "
            f"[ENV: {env.upper()}, Execution Date: {report_date}, SOURCE: {source}]"
        )
        mail_config[
            "body"
        ] = f"""Hi Team,

User Consumption Feed has errors. Please see details below:

{message}

Thanks & Regards,
"""
    else:
        mail_config[
            "subject"
        ] = (
            f"SUCCESS: User Consumption Feed completed "
            f"[ENV: {env.upper()}, Execution Date: {report_date}, SOURCE: {source}]"
        )
        mail_config[
            "body"
        ] = f"""Hi,

User Consumption Feed completed without errors. Please see details below:

{message}

Thanks & Regards,
"""

    if file:
        mail_config["attach"].append(file)

    mail_status, mail_resp = email.send_email(mail_config)

    if mail_status is True:
        log.info(mail_resp)
    else:
        log.error(mail_resp)

    return


def read_json_config(base_path):
    json_config = (
        f"{base_path}/cittidigital/cd_user_consumption/config/gen_user_consumption.json"
    )

    try:
        with open(json_config, "r") as config_file:
            data = json.load(config_file)
    except Exception as e:
        raise Exception("Error while reading config file: " + str(e))

    return data


def get_db_details_impala(db_conf, env):
    home_dir = os.environ["HOME"]
    base_path = os.path.join(os.environ["HOME"], ".schema_browser/config/")
    api_env = "prod" if env in ["prod", "cob", "sb_prod", "sb_cob"] else env.split("_")[-1]

    command = (
        f"cpc_decrypt cdsd_de/{base_path}/cd_user_consumption/config/consumption_stats "
        f"{db_conf.get('UID')}"
    )
    p = subprocess.Popen(command, stdout=subprocess.PIPE, shell=True)
    # impala_connect_pswd_decr = p.stdout.decode("utf-8").strip()
    impala_connect_pswd_decr = os.getenv("SECRET_IMPLA_NICK_NAME")

    if impala_connect_pswd_decr:
        return {
            "properties": {
                "username": db_conf.get("UID"),
                "password": impala_connect_pswd_decr.strip(),
                "ENV": api_env,  # hard coding for copying prod data into uat
                "datasource": "STARBURST",
                "upperCaseColumns": "",
                "queryTimeout": "3000",
            },
            "classname": "net.citi.olympus.jdbc.driver.OlympusDriver",
            "url": "jdbc:olympus",
            "jarFile": os.path.join(base_path, "olympus-jdbc-driver-2.0.10.jar"),
        }
    else:
        error = "secret impla nick name is empty"
        log.error(f"Failed to Decrypt the password. Error: {error}")
        return None


def get_db_details(db_conf, conn_type=None):
    home_dir = os.environ["HOME"]
    oraas_connect_str, oraas_connect_user = None, None

    if conn_type == "SRC":
        oraas_connect_str = db_conf.get("ora_str")
        oraas_connect_user = db_conf.get("SRC_ORAAS_USER")
    elif conn_type == "TGT":
        oraas_connect_str = db_conf.get("tgt_ora_str")
        oraas_connect_user = db_conf.get("TGT_ORAAS_USER")
    elif conn_type is None:
        return None

    oraas_connect_pswd_decr = os.getenv("SECRET_NICK_NAME")

    if oraas_connect_pswd_decr:
        connection_str = (
            oraas_connect_user
            + "/"
            + oraas_connect_pswd_decr.strip()
            + "@"
            + oraas_connect_str
        )
        return connection_str
    else:
        error = "secret nick name is empty"
        log.error(f"Failed to Decrypt the password. Error: {error}")
        return None


def read_input_params():
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--env",
        required=False,
        help="Environment to run the script (dev, uat, prod)",
    )
    parser.add_argument(
        "--api_source",
        required=False,
        help="Environment to run the script (rest, jdbc, bulk)",
    )
    parser.add_argument(
        "--from_date",
        required=False,
        help="Start date [yyyy-mm-dd] to extract API logs (inclusive)",
    )
    parser.add_argument(
        "--to_date",
        required=False,
        help="To date [yyyy-mm-dd] to extract API logs (inclusive)",
    )
    args = vars(parser.parse_args())

    env = os.getenv("env")
    api_source = "rest"

    today = datetime.date.today()
    current_date = today.strftime("%Y-%m-%d")

    if args["from_date"] is not None and args["to_date"] is not None:
        f_flag = validate_date("from_date", args["from_date"])
        t_flag = validate_date("to_date", args["to_date"])
        if f_flag is True and t_flag is True:
            from_date = args["from_date"]
            to_date = (
                datetime.datetime.strptime(args["to_date"], "%Y-%m-%d")
                + datetime.timedelta(days=7)
            ).strftime("%Y-%m-%d")
        else:
            raise Exception("Exiting...")
    elif args["from_date"] is None and args["to_date"] is not None:
        t_flag = validate_date("to_date", args["to_date"])
        if t_flag is True:
            to_date = args["to_date"]
            from_date = (
                datetime.datetime.strptime(args["to_date"], "%Y-%m-%d")
                - datetime.timedelta(days=7)
            ).strftime("%Y-%m-%d")
        else:
            raise Exception("Exiting...")
    else:
        to_date = (today - datetime.timedelta(days=1)).strftime("%Y-%m-%d")
        from_date = (today - datetime.timedelta(days=7)).strftime("%Y-%m-%d")
        # TODO: Change this to 7 during PROD
        # from_date = (today - datetime.timedelta(days=3)).strftime('%Y-%m-%d')

    if "api_source" in args.keys():
        if args["api_source"] not in ["rest", "jdbc", "bulk"]:
            raise Exception("Please provide a valid value for --api_source")
        api_source = args["api_source"]

    return env, from_date, to_date, current_date, api_source


def validate_date(date_type, date_arg):
    valid_params = True
    try:
        year, month, day = date_arg.split("-")
        datetime.datetime(int(year), int(month), int(day))
        datetime.datetime.strptime(date_arg, "%Y-%m-%d")
    except ValueError as e:
        raise Exception(
            f"Invalid date ({date_type}) provided. Please provide correct date in YYYY-MM-DD format: {e}"
        )
    except Exception as e:
        today = datetime.datetime.today()
        if date_type == "from_date":
            if today < datetime.datetime.strptime(date_arg, "%Y-%m-%d"):
                log.exception("FROM_DATE must not be greater than current date")
                valid_params = False

    return valid_params


def get_oraas_connection(connection_str):
    connection = None
    try:
        connection = cx_Oracle.connect(connection_str)
    except Exception as e:
        log.error(f"Error while connecting to ORAAS: {e}")
    return connection


def get_impala_connection(connection_str, db_conf):
    connection = None
    try:
        connection = jaydebeapi.connect(
            connection_str["classname"],
            connection_str["url"],
            connection_str["properties"],
            connection_str["jarFile"],
        )
    except Exception as e:
        log.exception(f"Error while connecting to IMPALA: {e}")
    return connection


def fetch_api_logs(conn_str, impala_conn_str, from_date, to_date, config, source):
    connection_cursor, db_conn = None, None

    if source == "rest":
        sql = f"""
        select client_user_id as USER_ID,
               log.status,
               start_time,
               end_time,
               request_json,
               plt.PLATFORM_NAME,
               API_NAME as API,
               ID_API_CALL_LOG
        from DALC_API_CALL_LOG log
          inner join DALC_CLIENT_API api
            on log.id_client_api = api.id_client_api
          left outer join DALC_CLIENT_APP app
            on log.id_client_app = app.id_client_app
          left outer join DALC_DATABASE_PLATFORM plt
            on api.query.ID_DATABASE_PLATFORM = plt.ID_DATABASE_PLATFORM
        where upper(API_NAME) like 'OLYMPUS%'
          and upper(API_NAME) NOT IN ('OLYMPUS_SQL_EXECUTE_NETEZZA', 'OLYMPUS_SQL_EXECUTE_NETEZZA_PRD')
          and to_char(trunc(received_time),'yyyy-mm-dd') >= '{from_date}'
          and to_char(trunc(received_time),'yyyy-mm-dd') <= '{to_date}'
        """

        try:
            db_conn = get_oraas_connection(conn_str)

            def output_type_handler(cursor, name, default_type, size, precision, scale):
                if default_type == cx_Oracle.DB_TYPE_CLOB:
                    return cursor.var(
                        cx_Oracle.LONG_STRING, arraysize=cursor.arraysize
                    )

            db_conn.outputtypehandler = output_type_handler
            connection_cursor = db_conn.cursor()
            connection_cursor.execute(sql)

        except cx_Oracle.DatabaseError as err:
            log.error("Error occured while fetching API logs " + str(err))
            return None, None

    elif source == "jdbc":
        dwh_business_dt = int(
            datetime.datetime.strptime(from_date, "%Y-%m-%d")
            .date()
            .strftime("%Y%m%d")
        )
        sql = f"""
        select log.client_id as USER_ID,
               log.status,
               st.start_time,
               et.end_time,
               log.query_text as request_json,
               comm.datasource_type as application_name,
               'JDBC API' as API,
               log.seq_id as ID
        from ofolvsd_standardization.jdbc_centralized_audit_log log
          inner join ofolvsd_standardization.jdbc_centralized_audit_common comm
            on log.request_id = comm.request_id
          inner join (
            select max(end_time) as end_time, request_id
            from ofolvsd_standardization.jdbc_centralized_audit_log
            where user_action = 'Completed Query Execution In-progress'
            group by request_id
          ) et
            on log.request_id = et.request_id
        where log.user_action = 'Entitled Query'
          and log.dwh_business_date = {dwh_business_dt}
          and comm.dwh_business_date = {dwh_business_dt}
          and log.start_time >= CAST('{from_date}' AS DATE)
          and log.start_time <= CAST('{to_date}' AS DATE)
        """

        try:
            db_conn = get_impala_connection(impala_conn_str, config)
            connection_cursor = db_conn.cursor()
            connection_cursor.execute(sql)
        except Exception as err:
            log.error("Error occured while fetching JDBC API logs " + str(err))
            return None, None

    elif source == "bulk":
        sql = f"""
        select case when RULE_ID = 'R1'
                    then FLOW || ' : ' || SOURCE_SYSTEM
                    else REQUESTED_BY
               end as USER_ID,
               STATUS,
               QUERY_START_TS as QUERY_EXECUTION_TIMESTAMP,
               QUERY_END_TS as QUERY_COMPLETION_TIMESTAMP,
               EXECUTED_QUERY as QUERY_TO_PARSE,
               DATA_SOURCE,
               'BULK API' as USAGE_METHOD,
               JOB_REQUEST_ID as API_ID
        from d3as_bulk_api_job_request
        where to_char(trunc(QUERY_START_TS),'yyyy-mm-dd') >= '{from_date}'
          and to_char(trunc(QUERY_START_TS),'yyyy-mm-dd') <= '{to_date}'
        """

        try:
            db_conn = get_oraas_connection(conn_str)
            connection_cursor = db_conn.cursor()
            connection_cursor.execute(sql)
        except Exception as err:
            log.error("Error occured while fetching API logs " + str(err))
            return None, None

    return connection_cursor, db_conn


def write_data_to_csv(final_usage_profile, table_cols_map):
    result_rows = []

    for each_row in final_usage_profile:
        (
            user_id,
            api_name,
            report_id,
            start_time,
            end_time,
            application_name,
            schema,
            table,
            column,
            usage,
        ) = each_row
        try:
            schema = schema.replace("'", "") if schema is not None else schema
            table = table.replace("'", "") if table is not None else table

            if column.upper() in ["TOP", "LIMIT", "ROW_NUMBER"]:
                continue

            if len(column) > 50:
                column = column[:50]

            if table and re.search(r"\.", table):
                for each_table_entry in table.split("."):
                    if re.match(r"\w+\.\w+", each_table_entry, re.IGNORECASE):
                        table_only = each_table_entry.split(".")[1]
                        if table_only in table_cols_map.keys():
                            if column in table_cols_map[table_only]["columns"]:
                                if len(table_cols_map[table_only]["schema"]) > 0:
                                    schema = list(
                                        table_cols_map[table_only]["schema"]
                                    )[0]
                                else:
                                    schema = None
                                table = table_only
                                result_rows.append(
                                    [
                                        user_id,
                                        api_name,
                                        schema,
                                        table,
                                        report_id,
                                        column,
                                        usage,
                                        start_time,
                                        end_time,
                                        application_name,
                                    ]
                                )
                            else:
                                continue
                        else:
                            continue
                    else:
                        continue

            elif table and re.match(r"\w+\.\w+", table, re.IGNORECASE):
                table_only = table.split(".")[1]
                if table_only in table_cols_map.keys():
                    if column in table_cols_map[table_only]["columns"]:
                        if len(table_cols_map[table_only]["schema"]) > 0:
                            schema = list(table_cols_map[table_only]["schema"])[0]
                        else:
                            schema = None
                        table = table_only
                        result_rows.append(
                            [
                                user_id,
                                api_name,
                                schema,
                                table,
                                report_id,
                                column,
                                usage,
                                start_time,
                                end_time,
                                application_name,
                            ]
                        )
                    else:
                        continue
                else:
                    continue

            elif table and table in table_cols_map.keys():
                if column in table_cols_map[table]["columns"]:
                    if len(table_cols_map[table]["schema"]) > 0:
                        schema = list(table_cols_map[table]["schema"])[0]
                    else:
                        schema = None
                    result_rows.append(
                        [
                            user_id,
                            api_name,
                            schema,
                            table,
                            report_id,
                            column,
                            usage,
                            start_time,
                            end_time,
                            application_name,
                        ]
                    )
        except Exception:
            continue

    return result_rows


def load_csv_into_database(conn_str, csv_file, table_name):
    ret = True
    oraas_conn = get_oraas_connection(conn_str)
    oraas_cursor = oraas_conn.cursor()
    oraas_cursor.setinputsizes(
        None,
        None,
        None,
        None,
        None,
        None,
        cx_Oracle.TIMESTAMP,
        cx_Oracle.TIMESTAMP,
        None,
    )

    if table_name == "OM_CATALOGUE_USER_CONSUMPTION":
        sql = """
        INSERT INTO OM_CATALOGUE_USER_CONSUMPTION(
            USER_ID,
            USAGE_METHOD,
            SCHEMA_NAME,
            TABLE_PHYSICAL_NAME,
            API_ID,
            FIELD_LOGICAL_NAME,
            USAGE_TYPE,
            QUERY_EXECUTION_TIMESTAMP,
            QUERY_COMPLETION_TIMESTAMP,
            DATA_SOURCE
        ) VALUES (
            :1,
            :2,
            :4,
            :5,
            :13,
            :6,
            :7,
            TO_TIMESTAMP(:8,'YYYY-MM-DD HH24:MI:SS.FF6'),
            TO_TIMESTAMP(:9,'YYYY-MM-DD HH24:MI:SS.FF6'),
            :10
        )
        """

    batch_size = 20000

    try:
        with open(csv_file, "r") as reader:
            csv_reader = csv.reader(reader)
            data = []

            for line in csv_reader:
                data.append(tuple(line))
                if len(data) % batch_size == 0:
                    oraas_cursor.executemany(sql, data)
                    oraas_conn.commit()
                    data = []

            if data:
                oraas_cursor.executemany(sql, data)
                oraas_conn.commit()

    except Exception as err:
        log.error(
            f"Error Occured while writing consumption data to {table_name}: {err}"
        )
        ret = False

    if oraas_cursor:
        oraas_cursor.close()
    if oraas_conn:
        oraas_conn.close()

    return ret


def read_tables_metadata(conn_str):
    sql = """
    SELECT upper(trim(tbl.DB_SCHEMA_NAME)) AS SCHEMA_NAME,
           upper(trim(tbl.TABLE_PHYSICAL_NAME)) AS TABLE_NAME,
           upper(trim(col.FIELD_PHYSICAL_NAME)) AS COL_NAME
    FROM om_catalogue_table tbl
         INNER JOIN om_catalogue_field col
           ON tbl.table_id_sk = col.table_id_sk
    WHERE upper(tbl.storage_type) = 'HIVE'
      AND tbl.IS_CURRENT_FLAG = 'Y'
    """

    results = {}

    try:
        oraas_conn = get_oraas_connection(conn_str)
        cursor = oraas_conn.cursor()
        cursor.execute(sql)
        rows = cursor.fetchall()

        for row in rows:
            if row[1] in results:
                results[row[1]]["columns"].add(row[2])
                results[row[1]]["schema"].add(row[0])
            else:
                results[row[1]] = {}
                results[row[1]]["columns"] = set()
                results[row[1]]["schema"] = set()
                results[row[1]]["columns"].add(row[2])
                results[row[1]]["schema"].add(row[0])

    except cx_Oracle.DatabaseError as err:
        log.error("Error occured while fetching API logs " + str(err))
    except Exception as err:
        log.error("Error occured while fetching API logs " + str(err))
    else:
        if cursor:
            cursor.close()
        if oraas_conn:
            oraas_conn.close()

    return results


def map_object_names(names, table_alias_map):
    for row in range(0, len(names)):
        if names[row][1] in table_alias_map:
            try:
                names[row][0] = table_alias_map[names[row][1]].split(".")[0]
                names[row][1] = table_alias_map[names[row][1]].split(".")[1]
            except Exception:
                log.error("Error occured while assigning alias")
                return None
    return names


def extract_columns_from_json(input_data):
    columns_usage_data = []

    if isinstance(input_data, dict):
        data = (
            input_data["request-payload"]
            if "request-payload" in input_data.keys()
            else input_data
        )

        if "source" in data and len(data["source"]) > 0:
            source = data["source"]
        else:
            source = None

        if "columns" in data and len(data["columns"]) > 0:
            for column in data["columns"]:
                row = [None, source, column, "SELECT"]
                columns_usage_data.append(row)

        if "valueCols" in data and len(data["valueCols"]) > 0:
            for element in data["valueCols"]:
                row = [None, source, element["field"], "AGGREGATE"]
                columns_usage_data.append(row)

        if "filters" in data and len(data["filters"]) > 0:
            for element in data["filters"]:
                row = [None, source, element["field"], "FILTER"]
                columns_usage_data.append(row)

        if "rowGroupCols" in data and len(data["rowGroupCols"]) > 0:
            for element in data["rowGroupCols"]:
                row = [None, source, element["field"], "GROUP BY"]
                columns_usage_data.append(row)

    elif isinstance(input_data, list):
        for element in input_data:
            data = (
                element["request-payload"]
                if "request-payload" in element.keys()
                else element
            )

            if "source" in element and len(element["source"]) > 0:
                source = element["source"]
            else:
                source = None

            if "columns" in element and len(element["columns"]) > 0:
                for column in element["columns"]:
                    row = [None, source, column, "SELECT"]
                    columns_usage_data.append(row)

            if "valueCols" in element and len(element["valueCols"]) > 0:
                for entry in element["valueCols"]:
                    row = [None, source, entry["field"], "AGGREGATE"]
                    columns_usage_data.append(row)

            if "filters" in element and len(element["filters"]) > 0:
                for entry in element["filters"]:
                    row = [None, source, entry["field"], "FILTER"]
                    columns_usage_data.append(row)

            if "rowGroupCols" in element and len(element["rowGroupCols"]) > 0:
                for entry in element["rowGroupCols"]:
                    row = [None, source, entry["field"], "GROUP BY"]
                    columns_usage_data.append(row)

    return columns_usage_data


def clean_stage(filename):
    if os.path.isfile(filename):
        os.remove(filename)
        log.info(f"File {filename} was successfully removed...")
    else:
        log.error(f"File {filename} does not exist and hence was not dropped")
    return


def clean_target(conn_str, today, api_source):
    ret = True

    sql = f"""
    DELETE FROM OM_CATALOGUE_USER_CONSUMPTION
    WHERE to_char(trunc(DWH_CREATED_TIMESTAMP),'yyyy-mm-dd') = '{today}'
    """

    if api_source == "jdbc":
        sql += " AND usage_method = 'JDBC API'"
    elif api_source == "bulk":
        sql += " AND usage_method = 'BULK_API'"
    else:
        sql += " AND usage_method NOT IN ('JDBC API','BULK_API')"

    try:
        oraas_conn = get_oraas_connection(conn_str)
        cursor = oraas_conn.cursor()
        cursor.execute(sql)
        oraas_conn.commit()
    except Exception as error:
        log.error(f"{error}")
        ret = False

    if cursor:
        cursor.close()
    if oraas_conn:
        oraas_conn.close()

    return ret


def main():
    global log

    base_dir = os.environ["HOME"]
    dt_string = datetime.datetime.now().strftime("%Y%m%d%H%M%S%f")

    env, from_date, to_date, current_date, api_source = read_input_params()

    config = read_json_config(base_dir)

    allowed_envs = config.get("env")
    if env not in allowed_envs:
        raise Exception(
            "Environment not defined. Please use one of the (dev, uat, prod)"
        )

    ignore_apis = config.get("ignore_api")
    env_config = config.get(env)
    emailer = env_config.get("email")
    emailer.update(
        {"report_date": current_date, "env": env, "source": api_source}
    )

    log_path = os.path.join(base_dir, env_config["log_path"])
    log_path = f"{base_dir}/cittidigital/cd_user_consumption/log"
    log_name = "user_consumption_" + dt_string + ".log"
    log_file = os.path.join(log_path, log_name)
    logger = Logger.LogUtil(logpath=log_file)
    log = logger.create_logger()

    log.info("Initiating environment variables")
    data_path = os.path.join(base_dir, env_config["data_dir"])
    data_path = f"{base_dir}/cittidigital/cd_user_consumption/log"
    csv_file_name = f"staging_user_consumption_{dt_string}.csv"
    csv_file_with_path = os.path.join(data_path, csv_file_name)

    log.info("Starting program execution.")
    start_t = time.time()
    log.info("Fetching database details")

    src_db_connection_string = get_db_details(env_config, conn_type="SRC")
    tgt_db_connection_string = get_db_details(env_config, conn_type="TGT")

    impala_conn_str = None
    if api_source == "jdbc":
        impala_conn_str = get_db_details_impala(env_config, env)
        if impala_conn_str is None:
            error_message = (
                "Error while obtaining Impala connection. Please check logs for details."
            )
            error_occurred = True
            emailer.update({"error": error_occurred, "message": error_message})
            send_mail(emailer)
            clean_stage(csv_file_with_path)
            sys.exit(1)

    error_message = ""
    error_occurred = False

    if src_db_connection_string is None or tgt_db_connection_string is None:
        error_message = (
            "Error while obtaining database connection. Please check logs for details."
        )
        error_occurred = True
        emailer.update({"error": error_occurred, "message": error_message})
        send_mail(emailer)
        clean_stage(csv_file_with_path)
        sys.exit(1)

    log.info("Database details retrieved successfully...")
    log.info(f"Reading source data from {from_date} to {to_date}")

    total_rows = 0
    error_rows = 0

    try:
        log.info("Preparing to clean target table...")
        status_c = clean_target(
            tgt_db_connection_string, current_date, api_source
        )
        if status_c is False:
            error_message = (
                "Error while deleting the target table. Please check logs for details."
            )
            error_occurred = True
            emailer.update({"error": error_occurred, "message": error_message})
            send_mail(emailer)
            clean_stage(csv_file_with_path)
            sys.exit(1)

        log.info("Target cleaned.")
        cursor, conn = fetch_api_logs(
            src_db_connection_string,
            impala_conn_str,
            from_date,
            to_date,
            env_config,
            api_source,
        )

        if cursor is None:
            error_message = (
                "Error while data from source table. Please check logs for details."
            )
            error_occurred = True
            emailer.update({"error": error_occurred, "message": error_message})
            send_mail(emailer)
            if conn:
                conn.close()
            clean_stage(csv_file_with_path)
            sys.exit(1)

        rows = cursor.fetchall()
        cursor.close()  # To Avoid ORA-01555 Error
        log.info("Data fetched from the API logs.")

        log.info("Fetching Tables metadata...")
        table_cols_map = read_tables_metadata(tgt_db_connection_string)

        if len(table_cols_map) == 0:
            error_message = (
                "Error while fetching tables metadata. Please check logs for details."
            )
            error_occurred = True
            emailer.update({"error": error_occurred, "message": error_message})
            send_mail(emailer)
            if conn:
                conn.close()
            clean_stage(csv_file_with_path)
            sys.exit(1)

        csv_file = open(csv_file_with_path, "w", newline="")
        csv_writer = csv.writer(csv_file)

        final_usage_profile = []
        total_rows = 0

        for row in rows:
            (
                user_id,
                status,
                start_time,
                end_time,
                req_json,
                application_name,
                api,
                api_id,
            ) = row
            total_rows += 1

            if req_json is None or len(req_json) > 150000:
                continue

            if user_id is None or len(user_id.strip()) == 0:
                user_id = "Unknown"

            if api_source == "rest":
                try:
                    data = json.loads(req_json)

                    if api.upper() in ignore_apis:
                        continue

                    if (
                        "request-payload" in data
                        and data["request-payload"] is not None
                        and "query" not in data["request-payload"]
                    ):
                        json_data = extract_columns_from_json(data)
                        if len(json_data) > 0:
                            for out_row in json_data:
                                final_usage_profile.append(
                                    tuple(
                                        [
                                            user_id.upper(),
                                            api.upper(),
                                            int(api_id),
                                            start_time,
                                            end_time,
                                            application_name,
                                        ]
                                        + out_row
                                    )
                                )

                    elif (
                        "request-payload" in data
                        and data["request-payload"]["query"] is not None
                    ):
                        try:
                            t = sql_parser.SqlParser(
                                data["request-payload"]["query"]
                            )
                            parsed_data = t.parse_sql()
                            if len(parsed_data) > 0:
                                for out_row in parsed_data:
                                    final_usage_profile.append(
                                        tuple(
                                            [
                                                user_id.upper(),
                                                api.upper()
                                                if api is not None
                                                else "",
                                                int(api_id),
                                                start_time,
                                                end_time,
                                                application_name,
                                            ]
                                            + out_row
                                        )
                                    )
                        except Exception:
                            error_rows += 1
                            continue

                except json.JSONDecodeError:
                    error_rows += 1
                    continue
                except Exception:
                    error_rows += 1
                    continue

            else:
                try:
                    if api_source == "bulk":
                        pattern2 = re.compile(
                            r"select\s+.*\s+from", re.IGNORECASE
                        )
                        req_json = pattern2.sub("SELECT a.* FROM", req_json)

                    if req_json.startswith("show partitions"):
                        continue

                    t = sql_parser.SqlParser(req_json)
                    parsed_data = t.parse_sql()
                    if len(parsed_data) > 0:
                        for out_row in parsed_data:
                            final_usage_profile.append(
                                tuple(
                                    [
                                        user_id.upper(),
                                        api.upper()
                                        if api is not None
                                        else "",
                                        int(api_id),
                                        start_time,
                                        end_time,
                                        application_name,
                                    ]
                                    + out_row
                                )
                            )
                except Exception:
                    error_rows += 1
                    continue

        if len(final_usage_profile) > 0:
            enriched_data = write_data_to_csv(
                final_usage_profile, table_cols_map
            )
            csv_writer.writerows(enriched_data)

        conn.close()
        csv_file.close()

        log.info(f"Total rows read from source : {total_rows}")
        log.info(f"Total error rows = {error_rows}")

        log.info(
            "Loading data into table OM_CATALOGUE_USER_CONSUMPTION..."
        )
        load_status = load_csv_into_database(
            tgt_db_connection_string,
            csv_file_with_path,
            "OM_CATALOGUE_USER_CONSUMPTION",
        )

        if load_status is False:
            error_message = (
                "Error occured while loading data into target table. "
                "Please check logs for details."
            )
            error_occurred = True
            emailer.update({"error": error_occurred, "message": error_message})
            send_mail(emailer)
            clean_stage(csv_file_with_path)
            sys.exit(1)

        # Remove CSV file after process completes
        clean_stage(csv_file_with_path)

    except Exception as e:
        error_message = f"Error occured while processing. {e}"
        log.error(error_message)
        error_occurred = True
        emailer.update({"error": error_occurred, "message": error_message})
        send_mail(emailer)
        clean_stage(csv_file_with_path)

    finally:
        end_t = time.time()
        elapsed_time = end_t - start_t

        if elapsed_time < 100:
            e_tim = str(round(elapsed_time)) + " seconds"
        else:
            e_tim = (
                str(round(elapsed_time // 60))
                + " minutes "
                + str(round(elapsed_time % 60))
                + " seconds"
            )

        log.info(f"Total program execution time: {e_tim}")
        if error_occurred is True:
            log.error(f"Process completed with errors. {error_message}")
            sys.exit(1)

        message = (
            f"Data Read for Period: {from_date} to {to_date}.\n"
            f"Total rows read from source: {total_rows}.\n"
            f"Rows ignored due to Errors: {error_rows}.\n"
            f"Total Elapsed Time: {e_tim}."
        )

        emailer.update({"error": error_occurred, "message": message})
        send_mail(emailer)
        log.info("Process completed successfully.")

        return


if __name__ == "__main__":
    main()
