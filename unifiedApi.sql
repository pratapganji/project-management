


SELECT *
FROM A167969NURAREC.ROLE_COLUMN_PERMISSION
WHERE UPPER(ROLE_NAME) = 'OLYMPUS_READ_WRITE'
  AND UPPER(TABLE_NAME) = 'OM_NURAFLOW_JIRA_TRACKING'
ORDER BY ID;



SELECT *
FROM A167969NURAREC.ROLE_TABLE_PERMISSION
WHERE UPPER(ROLE_NAME) = 'OLYMPUS_READ_WRITE'
  AND UPPER(TABLE_NAME) = 'OM_NURAFLOW_JIRA_TRACKING'
ORDER BY ID;





SELECT *
FROM A167969NURAREC.ROLE_COLUMN_PERMISSION
WHERE UPPER(ROLE_NAME) = 'OLYMPUS_READ_WRITE'
  AND UPPER(TABLE_NAME) = 'OM_NURAFLOW_JIRA_TRACKING'
ORDER BY ID;



SELECT *
FROM A167969NURAREC.ROLE_TABLE_PERMISSION
WHERE UPPER(ROLE_NAME) = 'OLYMPUS_READ_WRITE'
  AND UPPER(TABLE_NAME) = 'OM_NURAFLOW_JIRA_TRACKING'
ORDER BY ID;





INSERT INTO A167969NURAREC.ROLE_COLUMN_PERMISSION
(
    ID,
    ROLE_NAME,
    TABLE_NAME,
    COLUMN_NAME,
    PERMISSION,
    CREATE_DATE,
    UPDATE_DATE,
    CREATED_BY,
    UPDATED_BY
)
VALUES
(
    7,
    'OLYMPUS_READ_WRITE',
    'OM_NURAFLOW_JIRA_TRACKING',
    'JIRA_TICKET_KEY',
    'UPDATE',
    SYSTIMESTAMP,
    SYSTIMESTAMP,
    'OLYMPUS-NURA_FLOW',
    'OLYMPUS-NURA_FLOW'
);

INSERT INTO A167969NURAREC.ROLE_COLUMN_PERMISSION
(
    ID,
    ROLE_NAME,
    TABLE_NAME,
    COLUMN_NAME,
    PERMISSION,
    CREATE_DATE,
    UPDATE_DATE,
    CREATED_BY,
    UPDATED_BY
)
VALUES
(
    8,
    'OLYMPUS_READ_WRITE',
    'OM_NURAFLOW_JIRA_TRACKING',
    'JIRA_STATUS',
    'UPDATE',
    SYSTIMESTAMP,
    SYSTIMESTAMP,
    'OLYMPUS-NURA_FLOW',
    'OLYMPUS-NURA_FLOW'
);







INSERT INTO A167969NURAREC.ROLE_TABLE_PERMISSION
(
    ID,
    ROLE_NAME,
    TABLE_NAME,
    PERMISSION,
    CREATE_DATE,
    UPDATE_DATE,
    CREATED_BY,
    UPDATED_BY
)
VALUES
(
    5,
    'OLYMPUS_READ_WRITE',
    'OM_NURAFLOW_JIRA_TRACKING',
    'INSERT',
    SYSTIMESTAMP,
    SYSTIMESTAMP,
    'OLYMPUS-NURA_FLOW',
    'OLYMPUS-NURA_FLOW'
);

INSERT INTO A167969NURAREC.ROLE_TABLE_PERMISSION
(
    ID,
    ROLE_NAME,
    TABLE_NAME,
    PERMISSION,
    CREATE_DATE,
    UPDATE_DATE,
    CREATED_BY,
    UPDATED_BY
)
VALUES
(
    6,
    'OLYMPUS_READ_WRITE',
    'OM_NURAFLOW_JIRA_TRACKING',
    'UPDATE',
    SYSTIMESTAMP,
    SYSTIMESTAMP,
    'OLYMPUS-NURA_FLOW',
    'OLYMPUS-NURA_FLOW'
);








SELECT
    fr.FID_NAME,
    fr.ROLE_NAME,
    rtp.TABLE_NAME,
    rtp.PERMISSION AS TABLE_PERMISSION
FROM A167969NURAREC.FID_ROLE fr
JOIN A167969NURAREC.ROLE_TABLE_PERMISSION rtp
    ON UPPER(rtp.ROLE_NAME) = UPPER(fr.ROLE_NAME)
WHERE UPPER(fr.FID_NAME) = 'OLYMPUS_FID'
  AND UPPER(rtp.TABLE_NAME) = 'VALIDATION_CONFIG_RULES'
ORDER BY fr.ROLE_NAME, rtp.PERMISSION;







SELECT
    fr.FID_NAME,
    fr.ROLE_NAME,
    rcp.TABLE_NAME,
    rcp.COLUMN_NAME,
    rcp.PERMISSION AS COLUMN_PERMISSION
FROM A167969NURAREC.FID_ROLE fr
JOIN A167969NURAREC.ROLE_COLUMN_PERMISSION rcp
    ON UPPER(rcp.ROLE_NAME) = UPPER(fr.ROLE_NAME)
WHERE UPPER(fr.FID_NAME) = 'OLYMPUS_FID'
  AND UPPER(rcp.TABLE_NAME) = 'VALIDATION_CONFIG_RULES'
ORDER BY fr.ROLE_NAME, rcp.COLUMN_NAME, rcp.PERMISSION;






CREATE TABLE A167969NURAREC.OM_NURAFLOW_JIRA_TRACKING
(
    ID                         NUMBER             NOT NULL,
    HOP_ID                     NUMBER(19)         NOT NULL,
    NR_PDA_NAME                VARCHAR2(500)      NOT NULL,
    RECON_RUN_DATE             NUMBER(8)          NOT NULL,
    L0_DOMAIN                  VARCHAR2(200),
    SOURCE_SYSTEM_NAME         VARCHAR2(200),
    DESTINATION_SYSTEM_NAME    VARCHAR2(200),
    REPORT_STREAM_NAME         VARCHAR2(100),
    TOTAL_BREAKS               NUMBER(19),
    PRODUCT_NM                 VARCHAR2(100),
    JIRA_TICKET_KEY            VARCHAR2(50)       NOT NULL,
    JIRA_STATUS                VARCHAR2(50)       NOT NULL,
    RAISED_BY                  VARCHAR2(100),
    DWH_CREATED_TIMESTAMP      TIMESTAMP(6),
    DWH_CREATED_BY             VARCHAR2(100),
    DWH_UPDATED_TIMESTAMP      TIMESTAMP(6),
    DWH_UPDATED_BY             VARCHAR2(100),
    DWH_EXPIRY_TIMESTAMP       TIMESTAMP(6)
);




SELECT owner, table_name
FROM all_tables
WHERE owner = 'A167969NURAREC'
  AND table_name = 'OM_NURAFLOW_JIRA_TRACKING';






private String normalizeTableForPermission(String table) {

    if (table == null || table.isBlank()) {
        throw new DataAccessException("Table name cannot be empty");
    }

    String normalizedTable = table.trim().replace("\"", "");

    String[] tableParts = normalizedTable.split("\\.");

    // Query contains only table name:
    // OM_NURAFLOW_JIRA_TRACKING
    if (tableParts.length == 1) {
        return tableParts[0].toLowerCase();
    }

    // Query contains schema.table:
    // A167969NURAREC.OM_NURAFLOW_JIRA_TRACKING
    if (tableParts.length == 2) {

        String schema = tableParts[0];
        String tableName = tableParts[1];

        if (!SCHEMA_NAME.equalsIgnoreCase(schema)) {
            throw new UnauthorizedException(
                    "Access denied for schema: " + schema
            );
        }

        return tableName.toLowerCase();
    }

    throw new DataAccessException(
            "Invalid table identifier: " + table
    );
}






    SELECT * FROM OM_NURAFLOW_JIRA_TRACKING
* ✅ select * from om_nuraflow_jira_tracking (lowercase)
* ✅ SeLeCt * FrOm OM_NURAFLOW_JIRA_TRACKING (mixed case)
* ✅ SELECT COUNT(*) FROM OM_NURAFLOW_JIRA_TRACKING
* ✅ SELECT 1 FROM DUAL



SELECT
    SYS_CONTEXT('USERENV', 'DB_NAME') AS DB_NAME,
    SYS_CONTEXT('USERENV', 'SERVICE_NAME') AS SERVICE_NAME,
    SYS_CONTEXT('USERENV', 'CURRENT_SCHEMA') AS CURRENT_SCHEMA,
    USER AS CONNECTED_USER
FROM DUAL;



SELECT *
FROM A167969NURAREC.OM_NURAFLOW_JIRA_TRACKING
FETCH FIRST 10 ROWS ONLY;

SELECT COUNT(*) AS RECORD_COUNT
FROM A167969NURAREC.OM_NURAFLOW_JIRA_TRACKING;


public void checkColumnAccess(
        String fid,
        List<String> roles,
        String table,
        String op,
        List<String> columns) {

    boolean wildcardAllowed =
            repo.columnAllowed(roles, table, "*", op);

    log.info(
            "Wildcard column authorization result. fid={}, roles={}, table={}, operation={}, allowed={}",
            fid,
            roles,
            table,
            op,
            wildcardAllowed
    );

    if (wildcardAllowed) {
        return;
    }

    for (String col : columns) {
        boolean allowed =
                repo.columnAllowed(roles, table, col, op);

        log.info(
                "Column authorization result. fid={}, roles={}, table={}, column={}, operation={}, allowed={}",
                fid,
                roles,
                table,
                col,
                op,
                allowed
        );

        if (!allowed) {
            throw new UnauthorizedException(
                    "FID " + fid +
                    " denied " + op +
                    " access on column: " + col +
                    " in table: " + table
            );
        }
    }
}






public boolean columnAllowed(
        List<String> roles,
        String table,
        String column,
        String perm) {

    try {
        String placeholders = String.join(
                ",",
                roles.stream().map(role -> "?").toList()
        );

        String sql =
                "SELECT COUNT(*) " +
                "FROM A167969NURAREC.ROLE_COLUMN_PERMISSION " +
                "WHERE UPPER(ROLE_NAME) IN (" + placeholders + ") " +
                "AND UPPER(TABLE_NAME) = ? " +
                "AND UPPER(COLUMN_NAME) = ? " +
                "AND UPPER(PERMISSION) = ?";

        List<Object> params = new ArrayList<>();

        roles.stream()
                .map(String::toUpperCase)
                .forEach(params::add);

        params.add(table.toUpperCase());
        params.add(column.toUpperCase());
        params.add(perm.toUpperCase());

        log.info(
                "Checking column permission. roles={}, parsedTable={}, column={}, permission={}, params={}",
                roles,
                table,
                column,
                perm,
                params
        );

        Integer count = jdbc.queryForObject(
                sql,
                Integer.class,
                params.toArray()
        );

        boolean allowed = count != null && count > 0;

        log.info(
                "Column permission result. table={}, column={}, permission={}, count={}, allowed={}",
                table,
                column,
                perm,
                count,
                allowed
        );

        return allowed;

    } catch (Exception e) {
        log.error(
                "Error executing column permission query. roles={}, table={}, column={}, permission={}",
                roles,
                table,
                column,
                perm,
                e
        );

        throw new DataAccessException(
                "Unable to verify role column permissions"
        );
    }
}







public void checkTableAccess(
        String fid,
        List<String> roles,
        String table,
        String op) {

    boolean allowed = repo.tableAllowed(roles, table, op);

    log.info(
            "Table authorization result. fid={}, roles={}, table={}, operation={}, allowed={}",
            fid, roles, table, op, allowed
    );

    if (!allowed) {
        throw new UnauthorizedException(
                "FID " + fid + " denied " + op +
                " access on table: " + table
        );
    }
}






public boolean tableAllowed(List<String> roles, String table, String perm) {
    try {
        String placeholders = String.join(
                ",",
                roles.stream().map(role -> "?").toList()
        );

        String sql =
                "SELECT COUNT(*) " +
                "FROM A167969NURAREC.ROLE_TABLE_PERMISSION " +
                "WHERE UPPER(ROLE_NAME) IN (" + placeholders + ") " +
                "AND UPPER(TABLE_NAME) = ? " +
                "AND UPPER(PERMISSION) = ?";

        List<Object> params = new ArrayList<>();
        roles.stream()
                .map(String::toUpperCase)
                .forEach(params::add);

        params.add(table.toUpperCase());
        params.add(perm.toUpperCase());

        log.info(
                "Checking table permission. roles={}, parsedTable={}, permission={}, params={}",
                roles, table, perm, params
        );

        Integer count = jdbc.queryForObject(
                sql,
                Integer.class,
                params.toArray()
        );

        boolean allowed = count != null && count > 0;

        log.info(
                "Table permission result. table={}, permission={}, count={}, allowed={}",
                table, perm, count, allowed
        );

        return allowed;

    } catch (Exception e) {
        log.error(
                "Error executing table permission query. roles={}, table={}, permission={}",
                roles, table, perm, e
        );

        throw new DataAccessException(
                "Unable to verify role table permission"
        );
    }
}








SELECT fr.FID_NAME,
       fr.ROLE_NAME,
       rtp.TABLE_NAME,
       rtp.PERMISSION AS TABLE_PERMISSION,
       rcp.COLUMN_NAME,
       rcp.PERMISSION AS COLUMN_PERMISSION
FROM A167969NURAREC.FID_ROLE fr
LEFT JOIN A167969NURAREC.ROLE_TABLE_PERMISSION rtp
       ON UPPER(rtp.ROLE_NAME) = UPPER(fr.ROLE_NAME)
LEFT JOIN A167969NURAREC.ROLE_COLUMN_PERMISSION rcp
       ON UPPER(rcp.ROLE_NAME) = UPPER(fr.ROLE_NAME)
      AND UPPER(rcp.TABLE_NAME) = UPPER(rtp.TABLE_NAME)
WHERE UPPER(fr.FID_NAME) = 'OLYMPUS_FID'
  AND UPPER(rtp.TABLE_NAME) = 'OM_NURAFLOW_JIRA_TRACKING';






SELECT COUNT(*) AS MATCH_COUNT
FROM A167969NURAREC.ROLE_COLUMN_PERMISSION
WHERE ROLE_NAME IN ('OLYMPUS_READ_ONLY', 'OLYMPUS_READ_WRITE')
  AND TABLE_NAME = 'om_nuraflow_jira_tracking'
  AND COLUMN_NAME = '*'
  AND PERMISSION = 'SELECT';



SELECT COUNT(*) AS MATCH_COUNT
FROM A167969NURAREC.ROLE_TABLE_PERMISSION
WHERE ROLE_NAME IN ('OLYMPUS_READ_ONLY', 'OLYMPUS_READ_WRITE')
  AND TABLE_NAME = 'om_nuraflow_jira_tracking'
  AND PERMISSION = 'SELECT';




UPDATE A167969NURAREC.ROLE_TABLE_PERMISSION
SET TABLE_NAME = 'om_nuraflow_jira_tracking',
    UPDATE_DATE = SYSTIMESTAMP,
    UPDATED_BY = 'OLYMPUS-NURA_FLOW'
WHERE ROLE_NAME = 'OLYMPUS_READ_WRITE'
  AND TABLE_NAME = 'OM_NURAFLOW_JIRA_TRACKING'
  AND PERMISSION = 'SELECT';

UPDATE A167969NURAREC.ROLE_COLUMN_PERMISSION
SET TABLE_NAME = 'om_nuraflow_jira_tracking',
    UPDATE_DATE = SYSTIMESTAMP,
    UPDATED_BY = 'OLYMPUS-NURA_FLOW'
WHERE ROLE_NAME = 'OLYMPUS_READ_WRITE'
  AND TABLE_NAME = 'OM_NURAFLOW_JIRA_TRACKING'
  AND COLUMN_NAME = '*'
  AND PERMISSION = 'SELECT';

COMMIT;





SELECT
    fr.FID_NAME,
    fr.ROLE_NAME,
    rtp.TABLE_NAME,
    rtp.PERMISSION AS TABLE_PERMISSION,
    rcp.COLUMN_NAME,
    rcp.PERMISSION AS COLUMN_PERMISSION
FROM A167969NURAREC.FID_ROLE fr
JOIN A167969NURAREC.ROLE_TABLE_PERMISSION rtp
    ON UPPER(rtp.ROLE_NAME) = UPPER(fr.ROLE_NAME)
LEFT JOIN A167969NURAREC.ROLE_COLUMN_PERMISSION rcp
    ON UPPER(rcp.ROLE_NAME) = UPPER(fr.ROLE_NAME)
   AND UPPER(rcp.TABLE_NAME) = UPPER(rtp.TABLE_NAME)
WHERE UPPER(fr.FID_NAME) = 'OLYMPUS_FID'
ORDER BY rtp.TABLE_NAME, fr.ROLE_NAME, rcp.COLUMN_NAME;




SELECT
    fr.FID_NAME,
    fr.ROLE_NAME,
    rcp.TABLE_NAME,
    rcp.COLUMN_NAME,
    rcp.PERMISSION
FROM A167969NURAREC.FID_ROLE fr
JOIN A167969NURAREC.ROLE_COLUMN_PERMISSION rcp
    ON UPPER(rcp.ROLE_NAME) = UPPER(fr.ROLE_NAME)
WHERE UPPER(fr.FID_NAME) = 'OLYMPUS_FID'
  AND UPPER(rcp.TABLE_NAME) = 'OM_NURAFLOW_JIRA_TRACKING'
  AND UPPER(rcp.PERMISSION) = 'SELECT';




SELECT
    fr.FID_NAME,
    fr.ROLE_NAME,
    rtp.TABLE_NAME,
    rtp.PERMISSION
FROM A167969NURAREC.FID_ROLE fr
JOIN A167969NURAREC.ROLE_TABLE_PERMISSION rtp
    ON UPPER(rtp.ROLE_NAME) = UPPER(fr.ROLE_NAME)
WHERE UPPER(fr.FID_NAME) = 'OLYMPUS_FID'
  AND UPPER(rtp.TABLE_NAME) = 'OM_NURAFLOW_JIRA_TRACKING'
  AND UPPER(rtp.PERMISSION) = 'SELECT';





SELECT
    fr.FID_NAME,
    fr.ROLE_NAME,
    rtp.TABLE_NAME,
    rtp.PERMISSION
FROM A167969NURAREC.FID_ROLE fr
JOIN A167969NURAREC.ROLE_TABLE_PERMISSION rtp
    ON UPPER(rtp.ROLE_NAME) = UPPER(fr.ROLE_NAME)
WHERE UPPER(fr.FID_NAME) = 'OLYMPUS_FID'
ORDER BY fr.ROLE_NAME, rtp.TABLE_NAME, rtp.PERMISSION;





UPDATE A167969NURAREC.ROLE_COLUMN_PERMISSION
SET TABLE_NAME  = 'om_nuraflow_jira_tracking',
    UPDATE_DATE = SYSTIMESTAMP,
    UPDATED_BY  = 'OLYMPUS-NURA_FLOW'
WHERE ID = 6
  AND ROLE_NAME = 'OLYMPUS_READ_WRITE'
  AND TABLE_NAME = 'OM_NURAFLOW_JIRA_TRACKING'
  AND COLUMN_NAME = '*'
  AND PERMISSION = 'SELECT';




UPDATE A167969NURAREC.ROLE_TABLE_PERMISSION
SET TABLE_NAME  = 'om_nuraflow_jira_tracking',
    UPDATE_DATE = SYSTIMESTAMP,
    UPDATED_BY  = 'OLYMPUS-NURA_FLOW'
WHERE ID = 4
  AND ROLE_NAME = 'OLYMPUS_READ_WRITE'
  AND TABLE_NAME = 'OM_NURAFLOW_JIRA_TRACKING'
  AND PERMISSION = 'SELECT';



SELECT
    '[' || ROLE_NAME || ']' AS ROLE_VALUE,
    LENGTH(ROLE_NAME) AS ROLE_LENGTH,
    '[' || TABLE_NAME || ']' AS TABLE_VALUE,
    LENGTH(TABLE_NAME) AS TABLE_LENGTH,
    '[' || PERMISSION || ']' AS PERMISSION_VALUE,
    LENGTH(PERMISSION) AS PERMISSION_LENGTH
FROM A167969NURAREC.ROLE_TABLE_PERMISSION
WHERE ID = 4;


SELECT *
FROM A167969NURAREC.ROLE_TABLE_PERMISSION
WHERE ROLE_NAME = 'OLYMPUS_READ_WRITE'
  AND TABLE_NAME = 'OM_NURAFLOW_JIRA_TRACKING'
  AND PERMISSION = 'SELECT';


SELECT *
FROM A167969NURAREC.ROLE_TABLE_PERMISSION
WHERE ROLE_NAME = 'OLYMPUS_READ_WRITE'
  AND TABLE_NAME = 'om_nuraflow_jira_tracking'
  AND PERMISSION = 'SELECT';


SELECT *
FROM A167969NURAREC.FID_ROLE
WHERE UPPER(FID_NAME) = 'OLYMPUS_FID';


SELECT *
FROM A167969NURAREC.ROLE_TABLE_PERMISSION
WHERE UPPER(ROLE_NAME) = 'OLYMPUS_READ_WRITE'
  AND UPPER(TABLE_NAME) = 'OM_NURAFLOW_JIRA_TRACKING'
  AND UPPER(PERMISSION) = 'SELECT';

SELECT *
FROM A167969NURAREC.ROLE_COLUMN_PERMISSION
WHERE UPPER(ROLE_NAME) = 'OLYMPUS_READ_WRITE'
  AND UPPER(TABLE_NAME) = 'OM_NURAFLOW_JIRA_TRACKING'
  AND COLUMN_NAME = '*'
  AND UPPER(PERMISSION) = 'SELECT';





SELECT *
FROM A167969NURAREC.ROLE_TABLE_PERMISSION
WHERE ID = 4;

SELECT *
FROM A167969NURAREC.ROLE_COLUMN_PERMISSION
WHERE ID = 6;





SELECT ID, COUNT(*) AS RECORD_COUNT
FROM A167969NURAREC.ROLE_TABLE_PERMISSION
WHERE ID = 4
GROUP BY ID;

SELECT ID, COUNT(*) AS RECORD_COUNT
FROM A167969NURAREC.ROLE_COLUMN_PERMISSION
WHERE ID = 6
GROUP BY ID;




SELECT *
FROM A167969NURAREC.ROLE_COLUMN_PERMISSION
WHERE UPPER(ROLE_NAME) = 'OLYMPUS_READ_WRITE'
  AND UPPER(TABLE_NAME) = 'OM_NURAFLOW_JIRA_TRACKING'
  AND COLUMN_NAME = '*'
  AND UPPER(PERMISSION) = 'SELECT';


SELECT *
FROM A167969NURAREC.ROLE_TABLE_PERMISSION
WHERE UPPER(ROLE_NAME) = 'OLYMPUS_READ_WRITE'
  AND UPPER(TABLE_NAME) = 'OM_NURAFLOW_JIRA_TRACKING'
  AND UPPER(PERMISSION) = 'SELECT';







SELECT ID, COUNT(*) AS RECORD_COUNT
FROM A167969NURAREC.ROLE_TABLE_PERMISSION
WHERE ID = 4
GROUP BY ID;

SELECT ID, COUNT(*) AS RECORD_COUNT
FROM A167969NURAREC.ROLE_COLUMN_PERMISSION
WHERE ID = 6
GROUP BY ID;





SELECT *
FROM A167969NURAREC.ROLE_COLUMN_PERMISSION
WHERE UPPER(ROLE_NAME) = 'OLYMPUS_READ_WRITE'
  AND UPPER(TABLE_NAME) = 'OM_NURAFLOW_JIRA_TRACKING'
  AND COLUMN_NAME = '*'
  AND UPPER(PERMISSION) = 'SELECT';




SELECT *
FROM A167969NURAREC.ROLE_TABLE_PERMISSION
WHERE UPPER(ROLE_NAME) = 'OLYMPUS_READ_WRITE'
  AND UPPER(TABLE_NAME) = 'OM_NURAFLOW_JIRA_TRACKING'
  AND UPPER(PERMISSION) = 'SELECT';






SAVEPOINT BEFORE_JIRA_PERMISSION;

------------------------------------------------------------
-- 1. Table-level SELECT permission
------------------------------------------------------------
INSERT INTO A167969NURAREC.ROLE_TABLE_PERMISSION
(
    ID,
    ROLE_NAME,
    TABLE_NAME,
    PERMISSION,
    CREATE_DATE,
    UPDATE_DATE,
    CREATED_BY,
    UPDATED_BY
)
SELECT
    4,
    'OLYMPUS_READ_WRITE',
    'OM_NURAFLOW_JIRA_TRACKING',
    'SELECT',
    SYSTIMESTAMP,
    SYSTIMESTAMP,
    'OLYMPUS-NURA_FLOW',
    'OLYMPUS-NURA_FLOW'
FROM DUAL
WHERE NOT EXISTS
(
    SELECT 1
    FROM A167969NURAREC.ROLE_TABLE_PERMISSION
    WHERE UPPER(ROLE_NAME) = 'OLYMPUS_READ_WRITE'
      AND UPPER(TABLE_NAME) = 'OM_NURAFLOW_JIRA_TRACKING'
      AND UPPER(PERMISSION) = 'SELECT'
);

------------------------------------------------------------
-- 2. All-column SELECT permission
------------------------------------------------------------
INSERT INTO A167969NURAREC.ROLE_COLUMN_PERMISSION
(
    ID,
    ROLE_NAME,
    TABLE_NAME,
    COLUMN_NAME,
    PERMISSION,
    CREATE_DATE,
    UPDATE_DATE,
    CREATED_BY,
    UPDATED_BY
)
SELECT
    6,
    'OLYMPUS_READ_WRITE',
    'OM_NURAFLOW_JIRA_TRACKING',
    '*',
    'SELECT',
    SYSTIMESTAMP,
    SYSTIMESTAMP,
    'OLYMPUS-NURA_FLOW',
    'OLYMPUS-NURA_FLOW'
FROM DUAL
WHERE NOT EXISTS
(
    SELECT 1
    FROM A167969NURAREC.ROLE_COLUMN_PERMISSION
    WHERE UPPER(ROLE_NAME) = 'OLYMPUS_READ_WRITE'
      AND UPPER(TABLE_NAME) = 'OM_NURAFLOW_JIRA_TRACKING'
      AND COLUMN_NAME = '*'
      AND UPPER(PERMISSION) = 'SELECT'
);














SELECT c.constraint_name,
       c.constraint_type,
       cc.column_name,
       cc.position
FROM all_constraints c
JOIN all_cons_columns cc
  ON cc.owner = c.owner
 AND cc.constraint_name = c.constraint_name
WHERE c.owner = 'A167969NURAREC'
  AND c.table_name = 'ROLE_TABLE_PERMISSION'
ORDER BY c.constraint_name, cc.position;


SELECT MAX(ID) AS MAX_ID
FROM A167969NURAREC.ROLE_COLUMN_PERMISSION;

SELECT MAX(ID) AS MAX_ID
FROM A167969NURAREC.ROLE_TABLE_PERMISSION;








SELECT *
FROM A167969NURAREC.ROLE_TABLE_PERMISSION
WHERE UPPER(TABLE_NAME) = 'OM_NURAFLOW_JIRA_TRACKING';

SELECT *
FROM A167969NURAREC.ROLE_COLUMN_PERMISSION
WHERE UPPER(TABLE_NAME) = 'OM_NURAFLOW_JIRA_TRACKING';

SELECT *
FROM A167969NURAREC.ROLE_ROW_RULE
WHERE UPPER(TABLE_NAME) = 'OM_NURAFLOW_JIRA_TRACKING';


SELECT *
FROM A167969NURAREC.ROLE_COLUMN_PERMISSION
WHERE ID = 5
  AND UPPER(ROLE_NAME) = 'OLYMPUS_READ_WRITE'
  AND LOWER(TABLE_NAME) = 'validation_config_rules'
  AND COLUMN_NAME = '*'
  AND UPPER(PERMISSION) = 'SELECT';





INSERT INTO A167969NURAREC.ROLE_COLUMN_PERMISSION
(
    ID,
    ROLE_NAME,
    TABLE_NAME,
    COLUMN_NAME,
    PERMISSION,
    CREATE_DATE,
    UPDATE_DATE,
    CREATED_BY,
    UPDATED_BY
)
SELECT
    5,
    'OLYMPUS_READ_WRITE',
    'validation_config_rules',
    '*',
    'SELECT',
    SYSTIMESTAMP,
    SYSTIMESTAMP,
    'OLYMPUS-NURA_FLOW',
    'OLYMPUS-NURA_FLOW'
FROM DUAL
WHERE NOT EXISTS
(
    SELECT 1
    FROM A167969NURAREC.ROLE_COLUMN_PERMISSION
    WHERE UPPER(ROLE_NAME) = 'OLYMPUS_READ_WRITE'
      AND LOWER(TABLE_NAME) = 'validation_config_rules'
      AND COLUMN_NAME = '*'
      AND UPPER(PERMISSION) = 'SELECT'
);


SELECT MAX(ID) AS MAX_ID
FROM A167969NURAREC.ROLE_COLUMN_PERMISSION;

SELECT *
FROM A167969NURAREC.ROLE_COLUMN_PERMISSION
WHERE UPPER(ROLE_NAME) = 'OLYMPUS_READ_WRITE'
  AND LOWER(TABLE_NAME) = 'validation_config_rules'
  AND COLUMN_NAME = '*'
  AND UPPER(PERMISSION) = 'SELECT';


SELECT *
FROM A167969NURAREC.ROLE_TABLE_PERMISSION
WHERE UPPER(ROLE_NAME) = 'OLYMPUS_READ_WRITE'
  AND LOWER(TABLE_NAME) = 'validation_config_rules'
  AND UPPER(PERMISSION) = 'SELECT';






SELECT MAX(ID) AS MAX_ID
FROM A167969NURAREC.ROLE_COLUMN_PERMISSION;



SELECT trigger_name,
       status,
       triggering_event
FROM all_triggers
WHERE table_owner = 'A167969NURAREC'
  AND table_name = 'ROLE_COLUMN_PERMISSION';


SELECT *
FROM A167969NURAREC.ROLE_COLUMN_PERMISSION
WHERE UPPER(ROLE_NAME) = 'OLYMPUS_READ_WRITE'
  AND LOWER(TABLE_NAME) = 'validation_config_rules'
  AND COLUMN_NAME = '*'
  AND UPPER(PERMISSION) = 'SELECT';



SELECT c.constraint_name,
       c.constraint_type,
       cc.column_name,
       cc.position
FROM all_constraints c
JOIN all_cons_columns cc
  ON cc.owner = c.owner
 AND cc.constraint_name = c.constraint_name
WHERE c.owner = 'A167969NURAREC'
  AND c.table_name = 'ROLE_COLUMN_PERMISSION'
ORDER BY c.constraint_name, cc.position;



SELECT sequence_owner,
       sequence_name,
       last_number
FROM all_sequences
WHERE sequence_owner = 'A167969NURAREC'
  AND (
       UPPER(sequence_name) LIKE '%ROLE%COLUMN%'
       OR UPPER(sequence_name) LIKE '%PERMISSION%'
  );


SELECT *
FROM (
    SELECT *
    FROM A167969NURAREC.ROLE_COLUMN_PERMISSION
    ORDER BY ID DESC
)
WHERE ROWNUM <= 10;



SELECT *
FROM A167969NURAREC.ROLE_COLUMN_PERMISSION
WHERE UPPER(TABLE_NAME) = 'VALIDATION_CONFIG_RULES'
  AND UPPER(PERMISSION) = 'SELECT'
  AND ROLE_NAME IN ('OLYMPUS_READ_ONLY', 'OLYMPUS_READ_WRITE');








SELECT ID,
       HIERARCHY_PATH,
       DATA_TYPE,
       ACTIVE_FLAG
FROM AI67969NURAREC.validation_config_rules
WHERE HIERARCHY_PATH = '2052A~Debt'
AND DATA_TYPE = 'generic_validation';



{
  "fid": "OLYMPUS_FID",
  "sql": "SELECT ACTIVE_FLAG FROM validation_config_rules WHERE HIERARCHY_PATH = '2052A~Debt' AND DATA_TYPE = 'generic_validation'"
}




NOT (
       (HIERARCHY_PATH = '2052a~Debt'  AND DATA_TYPE = 'hop_validation')
    OR (HIERARCHY_PATH = '2052a~Loans' AND DATA_TYPE = 'generic_validation')
)


SELECT
    '[' || HIERARCHY_PATH || ']' AS HP,
    '[' || DATA_TYPE || ']' AS DT
FROM A167969NURAREC.validation_config_rules
WHERE HIERARCHY_PATH LIKE '%2052a%'



SELECT *
FROM A167969NURAREC.validation_config_rules
WHERE
(
    (
        HIERARCHY_PATH = '2052a~Debt'
        AND DATA_TYPE = 'hop_validation'
    )
    OR
    (
        HIERARCHY_PATH = '2052a~Loans'
        AND DATA_TYPE = 'generic_validation'
    )
)
AND
NOT
(
    (
        HIERARCHY_PATH = '2052a~Debt'
        AND DATA_TYPE = 'hop_validation'
    )
    OR
    (
        HIERARCHY_PATH = '2052a~Loans'
        AND DATA_TYPE = 'generic_validation'
    )
);


String normalizedSql = sql.toLowerCase();

if (
    normalizedSql.matches("(?s).*(--|/\\*|\\*/).*")
    ||
    normalizedSql.matches("(?s).*(['\"])(\\w+)\\1\\s*=\\s*\\1\\2\\1.*")
       || 
       normalizedSql.matches("(?s).*\\b(\\d+)\\s*=\\s*\\1\\b.*"))
)
