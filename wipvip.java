insert_job: 167969_OM_CRM_OLYMPUS_API_NEW_SERVER1_STOP   job_type: CMD
command: /data/1/gfolycmn/bin/common/current/scripts/olympus_jobLauncher.ksh instance_unique_name=${AUTO_JOB_NAME}
machine: sd-81f4-a5b3.nam.nsroot.net
owner: gfolybkb
permission:
date_conditions: 1
days_of_week: sa
start_times: "21:55"
description: "Stops the Olympus CRM OLYMPUS API NEW Server 1 instance"
n_retrys: 10
std_out_file: "${AUTOSYS_LOGDIR}/${AUTO_JOB_NAME}_${AUTOSYS_TIME}.out"
std_err_file: "${AUTOSYS_LOGDIR}/${AUTO_JOB_NAME}_${AUTOSYS_TIME}.err"
alarm_if_fail: 0
profile: "/data/1/gfolycmn/bin/common/current/scripts/nonSpark_batchProfile_basic.ksh"
alarm_if_terminated: 1


insert_job: 167969_OM_CRM_OLYMPUS_API_NEW_SERVER2_STOP   job_type: CMD
command: /data/1/gfolycmn/bin/common/current/scripts/olympus_jobLauncher.ksh instance_unique_name=${AUTO_JOB_NAME}
machine: sd-81f4-a5b3.nam.nsroot.net
owner: gfolybkb
permission:
date_conditions: 1
days_of_week: sa
start_times: "21:55"
description: "Stops the Olympus CRM OLYMPUS API NEW Server 2 instance"
n_retrys: 10
std_out_file: "${AUTOSYS_LOGDIR}/${AUTO_JOB_NAME}_${AUTOSYS_TIME}.out"
std_err_file: "${AUTOSYS_LOGDIR}/${AUTO_JOB_NAME}_${AUTOSYS_TIME}.err"
alarm_if_fail: 0
profile: "/data/1/gfolycmn/bin/common/current/scripts/nonSpark_batchProfile_basic.ksh"
alarm_if_terminated: 1


insert_job: 167969_OM_CRM_OLYMPUS_API_NEW_SERVER3_STOP   job_type: CMD
command: /data/1/gfolycmn/bin/common/current/scripts/olympus_jobLauncher.ksh instance_unique_name=${AUTO_JOB_NAME}
machine: sd-81f4-a5b3.nam.nsroot.net
owner: gfolybkb
permission:
date_conditions: 1
days_of_week: sa
start_times: "21:55"
description: "Stops the Olympus CRM OLYMPUS API NEW Server 3 instance"
n_retrys: 10
std_out_file: "${AUTOSYS_LOGDIR}/${AUTO_JOB_NAME}_${AUTOSYS_TIME}.out"
std_err_file: "${AUTOSYS_LOGDIR}/${AUTO_JOB_NAME}_${AUTOSYS_TIME}.err"
alarm_if_fail: 0
profile: "/data/1/gfolycmn/bin/common/current/scripts/nonSpark_batchProfile_basic.ksh"
alarm_if_terminated: 1


insert_job: 167969_OM_CRM_OLYMPUS_API_NEW_SERVER1_START   job_type: CMD
command: /data/1/gfolycmn/bin/common/current/scripts/olympus_jobLauncher.ksh instance_unique_name=${AUTO_JOB_NAME}
machine: sd-81f4-a5b3.nam.nsroot.net
owner: gfolybkb
permission:
date_conditions: 1
days_of_week: su
start_times: "13:12"
description: "Starts the Olympus CRM OLYMPUS API NEW Server 1 instance"
n_retrys: 10
std_out_file: "${AUTOSYS_LOGDIR}/${AUTO_JOB_NAME}_${AUTOSYS_TIME}.out"
std_err_file: "${AUTOSYS_LOGDIR}/${AUTO_JOB_NAME}_${AUTOSYS_TIME}.err"
alarm_if_fail: 0
profile: "/data/1/gfolycmn/bin/common/current/scripts/nonSpark_batchProfile_basic.ksh"
alarm_if_terminated: 1


insert_job: 167969_OM_CRM_OLYMPUS_API_NEW_SERVER2_START   job_type: CMD
command: /data/1/gfolycmn/bin/common/current/scripts/olympus_jobLauncher.ksh instance_unique_name=${AUTO_JOB_NAME}
machine: sd-81f4-a5b3.nam.nsroot.net
owner: gfolybkb
permission:
date_conditions: 1
days_of_week: su
start_times: "13:12"
description: "Starts the Olympus CRM OLYMPUS API NEW Server 2 instance"
n_retrys: 10
std_out_file: "${AUTOSYS_LOGDIR}/${AUTO_JOB_NAME}_${AUTOSYS_TIME}.out"
std_err_file: "${AUTOSYS_LOGDIR}/${AUTO_JOB_NAME}_${AUTOSYS_TIME}.err"
alarm_if_fail: 0
profile: "/data/1/gfolycmn/bin/common/current/scripts/nonSpark_batchProfile_basic.ksh"
alarm_if_terminated: 1


insert_job: 167969_OM_CRM_OLYMPUS_API_NEW_SERVER3_START   job_type: CMD
command: /data/1/gfolycmn/bin/common/current/scripts/olympus_jobLauncher.ksh instance_unique_name=${AUTO_JOB_NAME}
machine: sd-81f4-a5b3.nam.nsroot.net
owner: gfolybkb
permission:
date_conditions: 1
days_of_week: su
start_times: "13:12"
description: "Starts the Olympus CRM OLYMPUS API NEW Server 3 instance"
n_retrys: 10
std_out_file: "${AUTOSYS_LOGDIR}/${AUTO_JOB_NAME}_${AUTOSYS_TIME}.out"
std_err_file: "${AUTOSYS_LOGDIR}/${AUTO_JOB_NAME}_${AUTOSYS_TIME}.err"
alarm_if_fail: 0
profile: "/data/1/gfolycmn/bin/common/current/scripts/nonSpark_batchProfile_basic.ksh"
alarm_if_terminated: 1



public Resource getCsv(Optional<List<CatalogueSundry>> rs,
                       Optional<List<CatalogueSourceSystem>> rs1,
                       Optional<List<CatalogueSourceSystemCoverage>> rs2,
                       String sourceTable,
                       String sourcePathLocation,
                       String destinationPathLocation,
                       String controlFileName,
                       ExportTypeDTO exportTypeDTO) throws Exception {

    LOGGER.info("Method in : getCsv");

    File file = new File(sourcePathLocation);
    int length = 0;

    char charQuote = CSVWriter.NO_QUOTE_CHARACTER;
    char delim = '|';

    CSVWriter csvWriter = null;

    try {
        Writer writer = Files.newBufferedWriter(file.toPath()); // NOSONAR
        LOGGER.info("Writing data in : getCsv");

        csvWriter = new CSVWriter(writer, delim, charQuote,
                CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                CSVWriter.DEFAULT_LINE_END); // NOSONAR

        List<String[]> data;

        LOGGER.info("SourceTable : {}", sourceTable);

        if ("SUNDRY".equalsIgnoreCase(sourceTable) && rs.isPresent()) {
            data = toStringArray(rs.get(), null, null, sourceTable);
            length = rs.get().size();

        } else if ("COVERAGE".equalsIgnoreCase(sourceTable) && rs2.isPresent()) {
            data = toStringArray(null, null, rs2, sourceTable);
            length = rs2.get().size();

        } else if ("SOURCE".equalsIgnoreCase(sourceTable) && rs1.isPresent()) {
            data = toStringArray(null, rs1, null, sourceTable);
            length = rs1.get().size();

        } else {
            LOGGER.info("Invalid SourceTable passed");
            throw new Exception("There is an exception in fetching sourceTable : " + sourceTable);
        }

        LOGGER.info("After Writing data in : getCsv");
        csvWriter.writeAll(data);
        csvWriter.close();

        // -------- CTL FILE --------
        File ctlFile = processDimControleFile(controlFileName, length, sourceTable);

        // -------- ZIP CREATION --------
        ByteArrayOutputStream zipOut = new ByteArrayOutputStream();

        try (ZipOutputStream zos = new ZipOutputStream(zipOut)) {

            // CSV file entry
            try (FileInputStream fis = new FileInputStream(file)) {
                zos.putNextEntry(new ZipEntry(file.getName()));
                fis.transferTo(zos);
                zos.closeEntry();
            }

            // CTL file entry
            try (FileInputStream fis = new FileInputStream(ctlFile)) {
                zos.putNextEntry(new ZipEntry(ctlFile.getName()));
                fis.transferTo(zos);
                zos.closeEntry();
            }
        }

        LOGGER.info("ZIP created successfully for table : {}", sourceTable);

        // -------- CLEANUP --------
        if (file.exists()) {
            file.delete();
        }
        if (ctlFile.exists()) {
            ctlFile.delete();
        }

        LOGGER.info("Record count Table : {} Length : {}", sourceTable, length);
        LOGGER.info("CSV + CTL zipped successfully for Table : {}", sourceTable);

        return new ByteArrayResource(zipOut.toByteArray()) {
            @Override
            public String getFilename() {
                return controlFileName + ".zip";
            }
        };

    } catch (IOException e) {

        if (file.exists()) {
            boolean deleted = file.delete();
            if (deleted) {
                LOGGER.info("File deleted successfully");
            }
        }

        LOGGER.error("Error exporting table : {}", sourceTable, e);
        exportTypeDTO.setErrMessage("ZIP creation failed, Contact API Team");
        sendFileExportEmailNotification(exportTypeDTO, sourceTable, length, file);

        throw new Exception("There is an exception in IO & SQL : " + e);

    } catch (Exception e) {

        if (file.exists()) {
            boolean deleted = file.delete();
            if (deleted) {
                LOGGER.info("File deleted successfully");
            }
        }

        LOGGER.error("Error for recon", e);
        exportTypeDTO.setErrMessage("ZIP creation failed, Contact API Team");
        sendFileExportEmailNotification(exportTypeDTO, sourceTable, length, file);

        throw new Exception("There is an exception in creating CSV : " + e);
    }
}
