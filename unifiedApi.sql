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
)
