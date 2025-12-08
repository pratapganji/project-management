# password is fetched from CyberArk in settings_dev.py
db_password = settings.CONFIG.get("db_pass")

if not db_password:
    log.error("Password not found in settings.CONFIG['db_pass']")
    return None

connection_str = f"{oraas_connect_user}/{db_password}@{oraas_connect_str}"
return connection_str