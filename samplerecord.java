INSERT INTO OM_NURAFLOW_AUDIT_DATA (
    ID,
    GLOBAL_TRANSACTION_ID,
    CALLER_SERVICE,
    CALLED_SERVICE,
    API_ENDPOINT,
    REQUEST_PAYLOAD,
    RESPONSE_PAYLOAD,
    STATUS,
    MESSAGE,
    DWH_CREATED_BY,
    DWH_CREATED_TIMESTAMP,
    DWH_UPDATED_BY,
    DWH_UPDATED_TIMESTAMP,
    DWH_UPDATED_REASON,
    HEADERS
) VALUES (
    1001,
    'Id-01-test-gtid',
    'scheduler',
    'template-api',
    '/bulk/send',
    '{"dataSource": "SPARKYAML", "fileName": "2052a_Deposits_20250331", "sql": "SELECT * FROM dual", "callbackUrl": "https://olympus-nura-flow-icg.isg-olympus.api-169079.apis.managedcgt35d.ecs.dyn.nsroot.net/olympus/nuraflow/api/callback", "s3path": "s3a://olympus-bulk-yaml-extraction-uatr/SparkAPI/Yaml-Bulk"}',
    NULL,
    'FAILED',
    'Simulated retry test',
    'user_test',
    SYSTIMESTAMP,
    NULL,
    NULL,
    NULL,
    '{x-citiportal-loginid=recon_olympus_break_fid, X-Global-Transaction-ID=Id-01-test-gtid, Content-Type=application/json, accept=application/json}'
);

INSERT INTO OM_NURAFLOW_AUDIT_DATA VALUES (
    1002,
    'Id-02-test-gtid',
    'scheduler',
    'template-api',
    '/bulk/send',
    '{"dataSource": "SPARKYAML", "fileName": "2052a_Deposits_20250331", "sql": "SELECT * FROM dual", "callbackUrl": "https://.../callback", "s3path": "s3a://.../Yaml-Bulk"}',
    NULL,
    'FAILED',
    'Retry attempt record 2',
    'user_test',
    SYSTIMESTAMP,
    NULL,
    NULL,
    NULL,
    '{X-Global-Transaction-ID=Id-02-test-gtid, Content-Type=application/json}'
);

INSERT INTO OM_NURAFLOW_AUDIT_DATA VALUES (
    1003,
    'Id-03-test-gtid',
    'scheduler',
    'template-api',
    '/bulk/send',
    '{"dataSource": "SPARKYAML", "fileName": "2052a_Deposits_20250331", "sql": "SELECT 1 FROM DUAL", "callbackUrl": "https://.../callback", "s3path": "s3a://.../Yaml-Bulk"}',
    NULL,
    'FAILED',
    'Record for testing retry loop',
    'user_test',
    SYSTIMESTAMP,
    NULL,
    NULL,
    NULL,
    '{X-Global-Transaction-ID=Id-03-test-gtid, Content-Type=application/json}'
);

INSERT INTO OM_NURAFLOW_AUDIT_DATA VALUES (
    1004,
    'Id-04-test-gtid',
    'scheduler',
    'template-api',
    '/bulk/send',
    '{"dataSource": "SPARKYAML", "fileName": "2052a_Deposits_20250331", "sql": "SELECT 2 FROM dual", "callbackUrl": "https://.../callback", "s3path": "s3a://.../Yaml-Bulk"}',
    NULL,
    'FAILED',
    'Record 4 for retry',
    'user_test',
    SYSTIMESTAMP,
    NULL,
    NULL,
    NULL,
    '{X-Global-Transaction-ID=Id-04-test-gtid, Content-Type=application/json}'
);

INSERT INTO OM_NURAFLOW_AUDIT_DATA VALUES (
    1005,
    'Id-05-test-gtid',
    'scheduler',
    'template-api',
    '/bulk/send',
    '{"dataSource": "SPARKYAML", "fileName": "2052a_Deposits_20250331", "sql": "SELECT 3 FROM dual", "callbackUrl": "https://.../callback", "s3path": "s3a://.../Yaml-Bulk"}',
    NULL,
    'FAILED',
    'Record 5 for retry testing',
    'user_test',
    SYSTIMESTAMP,
    NULL,
    NULL,
    NULL,
    '{X-Global-Transaction-ID=Id-05-test-gtid, Content-Type=application/json}'
);