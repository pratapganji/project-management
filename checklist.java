

String existingWhere = where == null ? null : where.toString();

if (existingWhere == null) {
    where = CCJSqlParserUtil.parseCondExpression("(" + rule + ")");
} else {
    where = CCJSqlParserUtil.parseCondExpression(
        "(" + existingWhere + ") AND (" + rule + ")"
    );
}




UPDATE A167969NURAREC.ROLE_ROW_RULE
SET RULE_EXPRESSION =
'((HIERARCHY_PATH = ''2052A~Debt'' AND DATA_TYPE = ''generic_validation'')
 OR
 (HIERARCHY_PATH = ''2052a~Loans'' AND DATA_TYPE = ''hop_validation''))'
WHERE ROLE_NAME = 'OLYMPUS_ABAC_LIMITED_ROLE'
  AND TABLE_NAME = 'validation_config_rules'
  AND PERMISSION = 'SELECT';

COMMIT;



DELETE FROM AL67969NURAREC.ROLE_ROW_RULE
WHERE ROLE_NAME = 'OLYMPUS_ABAC_LIMITED_ROLE';

COMMIT;



INSERT INTO AL67969NURAREC.ROLE_ROW_RULE
(
 ROLE_NAME,
 TABLE_NAME,
 PERMISSION,
 RULE_EXPRESSION
)
VALUES
(
 'OLYMPUS_ABAC_LIMITED_ROLE',
 'validation_config_rules',
 'SELECT',
 '((HIERARCHY_PATH = ''2052a-Debt'' AND DATA_TYPE = ''generic_validation'')
   OR
  (HIERARCHY_PATH = ''2052a-Loans'' AND DATA_TYPE = ''hop_validation''))'
);

COMMIT;





INSERT INTO AL67969NURAREC.ROLE_ROW_RULE
(
 ROLE_NAME,
 TABLE_NAME,
 PERMISSION,
 RULE_EXPRESSION
)
VALUES
(
 'OLYMPUS_ABAC_LIMITED_ROLE',
 'validation_config_rules',
 'SELECT',
 '((HIERARCHY_PATH = ''2052a-Debt'' AND DATA_TYPE = ''generic_validation'')
   OR
  (HIERARCHY_PATH = ''2052a-Loans'' AND DATA_TYPE = ''hop_validation''))'
);

COMMIT;



DELETE FROM AL67969NURAREC.ROLE_ROW_RULE
WHERE ROLE_NAME = 'OLYMPUS_ABAC_LIMITED_ROLE';

COMMIT;





DELETE FROM A167969NURAREC.ROLE_ROW_RULE
WHERE ID = 42;

COMMIT;


SELECT *
FROM A167969NURAREC.ROLE_ROW_RULE
WHERE ROLE_NAME = 'OLYMPUS_ABAC_LIMITED_ROLE';



INSERT INTO A167969NURAREC.ROLE_ROW_RULE
(
  ROLE_NAME,
  TABLE_NAME,
  PERMISSION,
  RULE_EXPRESSION,
  CREATE_DATE,
  UPDATE_DATE,
  CREATED_BY,
  UPDATED_BY
)
VALUES
(
  'OLYMPUS_ABAC_LIMITED_ROLE',
  'validation_config_rules',
  'SELECT',
  '(HIERARCHY_PATH = ''2052A~Debt'' AND DATA_TYPE = ''generic_validation'') OR (HIERARCHY_PATH = ''2052a~Loans'' AND DATA_TYPE = ''hop_validation'')',
  SYSDATE,
  SYSDATE,
  'OLYMPUS-NURA_FLOW',
  'OLYMPUS-NURA_FLOW'
);

COMMIT;


SELECT *
FROM A167969NURAREC.ROLE_ROW_RULE
WHERE ROLE_NAME = 'OLYMPUS_ABAC_LIMITED_ROLE';


INSERT INTO ROLE_ROW_RULE
(
 ROLE_NAME,
 TABLE_NAME,
 PERMISSION,
 RULE_EXPRESSION
)
VALUES
(
 'OLYMPUS_ABAC_LIMITED_ROLE',
 'validation_config_rules',
 'SELECT',
 '(HIERARCHY_PATH = ''2052A~Debt''
   AND DATA_TYPE = ''generic_validation'')
 OR
 (HIERARCHY_PATH = ''2052a~Loans''
   AND DATA_TYPE = ''hop_validation'')'
);




https://artifactrepository.citigroup.net/ui/native/docker-enterprise-prod-local/developersvcs-python-ai/redhat-python-rhel8