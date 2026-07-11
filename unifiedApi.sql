
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
