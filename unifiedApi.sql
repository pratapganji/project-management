
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
