package com.mycompany.project_management.constants;

import org.springframework.stereotype.Component;

@Component
public class NuraSqlConstants {

    static final String AUDIT_BULK_API_FAILED_SQL = "SELECT * FROM OM_NURAFLOW_AUDIT_DATA WHERE STATUS = 'FAILED' AND CALLER_ = 'Template API'";
    static final String  AUDIT_BULK_API_UPDATE_SQL = "UPDATE OM_NURAFLOW_AUDIT_DATA SET STATUS = ?, RESPONSE_PAYLOAD = ? WHERE ID = ?";
}
