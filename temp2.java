from data import settings_dev as conf  # make sure this import is at the top of the file
# (add it near your other imports if not already there)

def get_db_details_impala(db_conf, env):
    """
    Build Impala/Starburst connection details.

    DB password is fetched in data/settings_dev.py from CyberArk/NGC
    and stored in conf.settings.CONFIG["db_pass"].
    """
    base_path = os.path.join(os.environ["HOME"], ".schema_browser/config/")
    api_env = "prod" if env in ["prod", "cob", "sb_prod", "sb_cob"] else env.split("_")[-1]

    db_password = conf.settings.CONFIG.get("db_pass")
    if not db_password:
        raise Exception(
            "DB password not found in settings.CONFIG['db_pass']. "
            "Check NGC secret fetch in data/settings_dev.py"
        )

    return {
        "properties": {
            "username": db_conf.get("UID"),
            "password": db_password,
            "ENV": api_env,          # hard coding for copying prod data into uat
            "datasource": "STARBURST",
            "upperCaseColumns": "",
            "queryTimeout": "3000",
        },
        "classname": "net.citi.olympus.jdbc.driver.OlympusDriver",
        "url": "jdbc:olympus",
        "jarfile": os.path.join(base_path, "olympus-jdbc-driver-2.0.10.jar"),
    }