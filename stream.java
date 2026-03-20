@Query(value = "SELECT * FROM om_catalogue_sundry m WHERE m.catalogue_sundry_sk IN ( " +
        "SELECT MAX(s.catalogue_sundry_sk) FROM om_catalogue_sundry s " +
        "WHERE s.status = 'COMPLETE' " +
        "GROUP BY s.dimension_group_name, s.source_code " +
        ") ORDER BY m.catalogue_sundry_sk",
        nativeQuery = true)
Stream<CatalogueSundry> streamAllLatestGroupedAndOrdered();

@Override
@Transactional
public Resource exportSundry(ExportTypeDTO exportTypeDTO) throws Exception {
    try {
        LOGGER.info("Method in : exportSundry");
        Stream<CatalogueSundry> sunList = Stream.empty();
        Timestamp lastRunTime = sdmlogRepository.getLastRunTime(sundryTable);
        LocalDateTime localDateTime = LocalDateTime.now(DateTimeZone.forID("America/New_York"));
        Timestamp currentTimeValue = new Timestamp(localDateTime.toDateTime().getMillis());

        String inputFileName = "CatalogueSundry";

        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        java.util.Date date = new java.util.Date();

        inputFileName = inputFileName + "_" + formatter.format(date) + ".csv";
        String sourcePathLocation = sundrySourcePath + inputFileName;
        String destinationPathLocation = sundryDestPath + inputFileName;

        Resource file;

        switch (exportTypeDTO.getType()) {
            case WEEKLY:
                sunList = catalogueSundryRepository.findByStatusAndIsCurrentFlag();
                LOGGER.info("export type 7 day load");
                break;

            case SPECIFIC:
                sunList = catalogueSundryRepository
                        .findByStatusAndIsCurrentFlagWithDays(exportTypeDTO.getNoOfDays());
                LOGGER.info("only " + exportTypeDTO.getNoOfDays() + " day load");
                break;

            case FULL_LOAD:
                try (Stream<CatalogueSundry> stream = catalogueSundryRepository.streamAllLatestGroupedAndOrdered()) {
                    file = getCsvStream(stream, null, null, "SUNDRY",
                            sourcePathLocation, destinationPathLocation,
                            "CatalogueSundry", exportTypeDTO);
                }
                if (file.contentLength() > 0) {

                    LOGGER.info("Updating SDM log");
                    Timestamp currentTimestamp = new Timestamp(
                            LocalDateTime.now(DateTimeZone.forID("America/New_York"))
                                    .toDateTime().getMillis()
                    );
                    CatalogueSdmLog sdmLog = setSdmLogDetails(
                            exportTypeDTO,
                            sundryTable,
                            currentTimestamp,
                            exportTypeDTO.getRecordCount() // from getCsv
                    );
                    sdmlogRepository.save(sdmLog);
                }

                LOGGER.info("Method out : exportSundry");
                LOGGER.info("full load streamed successfully");
                return file;
                //break;
        }

        file = getCsv(sunList, null, null, "SUNDRY", sourcePathLocation, destinationPathLocation, "CatalogueSundry", exportTypeDTO);

        if (file.contentLength() > 0 && sunList.isPresent()) {
            LOGGER.info("Updating the sdm log table _ lastSuccessfulRunTimestamp");
            LocalDateTime = LocalDateTime.now(DateTimeZone.forID("America/New_York"));
            Timestamp currentTimestamp = new Timestamp(localDateTime.toDateTime().getMillis());

            String queryLog = "Current Query fetch time range : between " + lastRunTime + " and " + currentTimeValue;
            LOGGER.info("Query Fetch timing:{}", queryLog);

            CatalogueSdmLog sdmLog = setSdmLogDetails(exportTypeDTO, sundryTable, currentTimestamp, sunList.get().size());
            sdmLogRepository.save(sdmLog);

            LOGGER.info("Inserted SUNDRY Log record into SDM LOG table successfully");
        }

        LOGGER.info("Method out : exportSundry");
        return file;
    }
    catch(Exception e){
        LOGGER.error("failed ", e);

    }
}

//ProcessDimCTLFile
public File processDimControleFile(String controlFileName, long recordsize, String sourceTable) throws Exception {
    LOGGER.info("Method in : processDimControleFile");

    String controlsourcePathLocation = StringUtils.EMPTY;

    SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
    java.util.Date date = new java.util.Date();
    controlFileName = controlFileName + "_" + formatter.format(date) + ".csv.ready";

    if ("SUNDRY".equalsIgnoreCase(sourceTable)) {
        LOGGER.info("ControlFile added for SUNDRY");
        controlsourcePathLocation = sundrySourcePath + controlFileName;

    } else if ("COVERAGE".equalsIgnoreCase(sourceTable)) {
        LOGGER.info("ControlFile added for Source system Coverage");
        controlsourcePathLocation = sourceDimPath + controlFileName;

    } else {
        LOGGER.info("ControlFile added for Source System");
        controlsourcePathLocation = sourceDimPath + controlFileName;
    }

    File file = new File(controlsourcePathLocation);

    try (Writer writer = Files.newBufferedWriter(file.toPath());
         CSVWriter csvWriter = new CSVWriter(writer, '|',
                 CSVWriter.NO_QUOTE_CHARACTER,
                 CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                 CSVWriter.DEFAULT_LINE_END)) {

        String count = Long.toString(recordsize);
        csvWriter.writeNext(new String[]{count});
    }

    LOGGER.info("Method out : processDimControleFile");
    return file;
}

//GetCSV as STREAM
public Resource getCsvStream(Stream<CatalogueSundry> rs,
                       Stream<CatalogueSourceSystem> rs1,
                       Stream<CatalogueSourceSystemCoverage> rs2,
                       String sourceTable,
                       String sourcePathLocation,
                       String destinationPathLocation,
                       String controlFileName,
                       ExportTypeDTO exportTypeDTO) throws Exception {

    LOGGER.info("Method in : getCsvStream");
    File file = new File(sourcePathLocation);
    int length = 0;
    char charQuote = CSVWriter.NO_QUOTE_CHARACTER;
    char delim = '|';
    CSVWriter csvWriter = null;

    try {
        Writer writer = Files.newBufferedWriter(file.toPath()); //NOSONAR
        LOGGER.info("Writing data in : getCsvStream");

        csvWriter = new CSVWriter(writer, delim, charQuote,
                CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                CSVWriter.DEFAULT_LINE_END); //NOSONAR

        List<String[]> data;
        LOGGER.info("SourceTable : " + sourceTable);

        if ("SUNDRY".equalsIgnoreCase(sourceTable) && rs.isPresent()) {
            length = writeToCsvStream(rs, null, null, sourceTable, csvWriter);
            //length = rs.get().size();

        } else if ("COVERAGE".equalsIgnoreCase(sourceTable) && rs2.isPresent()) {
            length = writeToCsvStream(null, null, rs2, sourceTable, csvWriter);
            //length = rs2.get().size();

        } else if ("SOURCE".equalsIgnoreCase(sourceTable) && rs1.isPresent()) {
            length = writeToCsvStream(null, rs1, null, sourceTable, csvWriter);
            //length = rs1.get().size();

        } else {
            data = new ArrayList<>();
            LOGGER.info("Invalid SourceTable passed");
            throw new Exception("There is an exception in fetching sourceTable: " + sourceTable);
        }

        LOGGER.info("After Writing data in : getCsv");
        //csvWriter.writeAll(data);
        csvWriter.close();

        File controlFile = processDimControleFile(controlFileName, length, sourceTable);

        ByteArrayOutputStream zipOut = new ByteArrayOutputStream();

        try (ZipOutputStream zos = new ZipOutputStream(zipOut)) {

            // CSV file entry
            try (FileInputStream fis = new FileInputStream(file)) {
                zos.putNextEntry(new ZipEntry(file.getName()));
                fis.transferTo(zos);
                zos.closeEntry();
            }

            // CTL file entry
            try (FileInputStream fis = new FileInputStream(controlFile)) {
                zos.putNextEntry(new ZipEntry(controlFile.getName()));
                fis.transferTo(zos);
                zos.closeEntry();
            }
        }

        LOGGER.info("ZIP created successfully for table : {}", sourceTable);

        // -------- CLEANUP --------
        if (file.exists()) {
            file.delete();
        }

        if (controlFile.exists()) {
            controlFile.delete();
        }

        LOGGER.info("Record count Table : {} Length : {}", sourceTable, length);
        LOGGER.info("CSV + CTL zipped successfully for Table : {}", sourceTable);

        LOGGER.info("Export file moved successfully for table " + sourceTable + " Sending notification mail ...");
        exportTypeDTO.setErrMessage("");
        sendFileExportEmailNotification(exportTypeDTO, sourceTable, length, file);

        return new ByteArrayResource(zipOut.toByteArray()) {
            @Override
            public String getFilename() {
                return controlFileName + ".zip";
            }
        };

    } 
    catch (IOException e) {
        if (file.exists()) {
            boolean deleted = file.delete();
            if (deleted) {
                LOGGER.info("File deleted successfully");
            }
        }

        LOGGER.info("Error exporting table : {}", sourceTable);
        LOGGER.info("There is issue to get result set & SQL exception csv file :{}", e.getLocalizedMessage());
        LOGGER.info("Full Error [SQLException] :{}", e.getMessage());
        exportTypeDTO.setErrMessage("File Move failed, Contact API Team");
        sendFileExportEmailNotification(exportTypeDTO, sourceTable, length, file);
        throw new Exception("There is an exception in IO & SQL " + e);

    } catch (Exception e) {
        if (file.exists()) {
            boolean deleted = file.delete();
            if (deleted) {
                LOGGER.info("File deleted successfully");
            }
        }

        LOGGER.info("Error for recon");
        LOGGER.info("There is issue to get data from Resultset :{}", e.getLocalizedMessage());
        LOGGER.info("Full Error [Exception] :{}", e.getMessage());
        exportTypeDTO.setErrMessage("File Move failed, Contact API Team");
        sendFileExportEmailNotification(exportTypeDTO, sourceTable, length, file);
        throw new Exception("There is an exception in creating csv :{}" + e);
    }
}

//Write CSV as Stream
private int writeToCsvStream(
        Stream<CatalogueSundry> sundry,
        Stream<CatalogueSourceSystem> source,
        Stream<CatalogueSourceSystemCoverage> sourceCoverage,
        String sourceTable,
        CSVWriter csvWriter) {

    LOGGER.info("Method in : writeToCsvStream");

    AtomicInteger counter = new AtomicInteger(0);

    // -------- SUNDRY --------
    if ("SUNDRY".equalsIgnoreCase(sourceTable) && sundry != null) {

        csvWriter.writeNext(new String[]{
                "SUNDRY_SK", "ACTION", "DIMENSION_GROUP_NAME", "ELEMENT_CODE", "ELEMENT_NAME",
                "REPORT_DESCRIPTION", "SOURCE_CODE", "SOURCE_DESCRIPTION", "IS_CURRENT_FLAG",
                "STATUS", "LABEL_ID_SK", "DWH_CREATED_BY", "DWH_CREATED_TIMESTAMP",
                "DWH_UPDATED_BY", "DWH_UPDATED_TIMESTAMP", "DWH_UPDATED_REASON", "ACTIVE_FLAG"
        });

        sundry.forEach(rs -> {
            csvWriter.writeNext(new String[]{
                    lineSepRem(rs.getSundrySk() + ""),
                    lineSepRem(rs.getAction()),
                    lineSepRem(rs.getDimensionGroupName()),
                    lineSepRem(rs.getElementCode()),
                    lineSepRem(rs.getElementName()),
                    lineSepRem(rs.getReportDescription()),
                    lineSepRem(rs.getSourceCode()),
                    lineSepRem(rs.getSourceDescription()),
                    lineSepRem(rs.getIsCurrentFlag()),
                    lineSepRem(rs.getStatus()),
                    lineSepRem(rs.getLabelId() + ""),
                    lineSepRem(rs.getCreatedBy()),
                    lineSepRem(rs.getCreatedDate() + ""),
                    lineSepRem(rs.getUpdatedBy()),
                    lineSepRem(rs.getUpdatedDate() + ""),
                    lineSepRem(rs.getUpdatedReason()),
                    lineSepRem(rs.getActiveFlag())
            });
            counter.incrementAndGet();
        });

    }
    // -------- COVERAGE --------
    else if ("COVERAGE".equalsIgnoreCase(sourceTable) && sourceCoverage != null) {

        csvWriter.writeNext(new String[]{
                "CATALOGUE_SOURCE_SYSTEM_COVERAGE_SK", "ACTION", "LABEL_ID_SK",
                "SOURCE_SYSTEM_COVERAGE_SK", "SOURCE_SYSTEM_SK", "CONCEPT_NAME",
                "SUBCONCEPT_NAME", "LIVE_DATE", "GRU_RECON_INDICATOR", "ACTIVE_FLAG",
                "IS_CURRENT_FLAG", "STATUS", "DWH_CREATED_BY", "DWH_CREATED_TIMESTAMP",
                "DWH_UPDATED_BY", "DWH_UPDATED_TIMESTAMP", "DWH_UPDATED_REASON",
                "ONBOARDING_REQUESTOR", "SOURCE_CONTACT", "SYNTHETIC_DATA_CONCEPT_L1",
                "SYNTHETIC_DATA_CONCEPT_L2", "SYNTHETIC_DATA_CONCEPT_L3",
                "SYNTHETIC_DATA_CONCEPT_L4", "SCHEMA_NAME", "TABLE_NAME",
                "SOURCE_SYSTEM_SK_IDENTIFIER_COLUMN", "CSI_IDENTIFIER_COLUMN",
                "BUSINESS_DATE_IDENTIFIER_COLUMN", "EOM_INDICATOR", "UNITY_INDICATOR",
                "EAGLE_DQ_INDICATOR", "GRU_RECON_IMPLEMENTATION_DATE",
                "EAGLE_DQ_IMPLEMENTATION_DATE", "EOM_IMPLEMENTATION_DATE",
                "UNITY_IMPLEMENTATION_DATE", "FLOW_RETIRE_DATE", "DATA_REFRESH_FREQUENCY",
                "OLYMPUS_GROUPING"
        });

        sourceCoverage.forEach(rs -> {

            String getLiveDate = lineSepRem(rs.getLiveDate() + "");
            String getRecon = lineSepRem(rs.getGruReconIndicator() + "");

            csvWriter.writeNext(new String[]{
                    lineSepRem(rs.getCatalogueSourceSk() + ""),
                    lineSepRem(rs.getAction()),
                    lineSepRem(rs.getLabelIdSk() + ""),
                    lineSepRem(rs.getSourceSystemCoverageSk() + ""),
                    lineSepRem(rs.getSourceSystemSk() + ""),
                    lineSepRem(rs.getConceptName()),
                    lineSepRem(rs.getSubConceptName()),
                    getLiveDate,
                    getRecon,
                    lineSepRem(rs.getActiveFlag()),
                    lineSepRem(rs.getIsCurrentFlag()),
                    lineSepRem(rs.getStatus()),
                    lineSepRem(rs.getDwhCreatedBy()),
                    lineSepRem(rs.getCreatedDate() + ""),
                    lineSepRem(rs.getUpdatedBy()),
                    lineSepRem(rs.getUpdatedDate() + ""),
                    lineSepRem(rs.getUpdatedReason()),
                    lineSepRem(rs.getOnboardingRequestor()),
                    lineSepRem(rs.getSourceContact()),
                    lineSepRem(rs.getSyntheticDataconceptl1()),
                    lineSepRem(rs.getSyntheticDataconceptl2()),
                    lineSepRem(rs.getSyntheticDataconceptl3()),
                    lineSepRem(rs.getSyntheticDataconceptl4()),
                    lineSepRem(rs.getSchemaName()),
                    lineSepRem(rs.getTableName()),
                    lineSepRem(rs.getSourceSystemSkIdentifierColumn()),
                    lineSepRem(rs.getCsiIdentifierColumn()),
                    lineSepRem(rs.getBusinessDateIdentifierColumn()),
                    lineSepRem(rs.getEomIndicator()),
                    lineSepRem(rs.getUnityIndicator()),
                    lineSepRem(rs.getEagleDqIndicator()),
                    lineSepRem(rs.getGruReconImplementationDate() + ""),
                    lineSepRem(rs.getEagleDqImplementationDate() + ""),
                    lineSepRem(rs.getEomImplementationDate() + ""),
                    lineSepRem(rs.getUnityImplementationDate() + ""),
                    lineSepRem(rs.getFlowRetireDate() + ""),
                    lineSepRem(rs.getDataRefreshFrequency()),
                    lineSepRem(rs.getOlympusGrouping())
            });

            counter.incrementAndGet();
        });

    }
    // -------- SOURCE --------
    else if ("SOURCE".equalsIgnoreCase(sourceTable) && source != null) {

        csvWriter.writeNext(new String[]{
                "CATALOGUE_SOURCE_SYSTEM_SK", "SOURCE_SYSTEM_SK", "ACTION", "ASSET_CLASS",
                "BUSINESS_REGION_CODE", "BUSINESS_REGION_SHORT_CODE", "COUNTRY_CODE",
                "CSI_ID", "DEFAULT_MPID", "FIRM_ID", "FLOW_DESCRIPTION", "IMPACTED_REPORT",
                "IS_FLOW_ACTIVE_FLAG", "OLYMPUS_DATA_FLOW_NAME", "SOURCE_SYSTEM_CODE",
                "SOURCE_SYSTEM_DESCRIPTION", "SOURCE_SYSTEM_ID", "SOURCE_SYSTEM_NAME",
                "SOURCE_SYSTEM_TIMEZONE", "SPECIFICATION_DESCRIPTION", "SYSTEM_FEED_FORMAT",
                "SYSTEM_TYPE", "LABEL_ID_SK", "IS_CURRENT_FLAG", "STATUS",
                "DWH_CREATED_BY", "DWH_CREATED_TIMESTAMP", "DWH_UPDATED_BY",
                "DWH_UPDATED_TIMESTAMP", "DWH_UPDATED_REASON", "PRIMARY_FLAG",
                "ACTIVE_FLAG", "DATA_OWNER_SOEID", "LINE_OF_BUSINESS"
        });

        source.forEach(rs -> {
            csvWriter.writeNext(new String[]{
                    lineSepRem(rs.getCatalogueSourceSk() + ""),
                    lineSepRem(rs.getSourceSystemSk() + ""),
                    lineSepRem(rs.getAction()),
                    lineSepRem(rs.getAssetClass()),
                    lineSepRem(rs.getBusinessRegionCode()),
                    lineSepRem(rs.getBusinessRegionShortCode()),
                    lineSepRem(rs.getCountryCode()),
                    lineSepRem(rs.getCsiId()),
                    lineSepRem(rs.getDefaultMpid()),
                    lineSepRem(rs.getFirmId()),
                    lineSepRem(rs.getFlowDescription()),
                    lineSepRem(rs.getImpactedReport()),
                    lineSepRem(rs.getIsFlowActiveFlag()),
                    lineSepRem(rs.getOlympusDataFlowName()),
                    lineSepRem(rs.getSourceSystemCode()),
                    lineSepRem(rs.getSourceSystemDescription()),
                    lineSepRem(rs.getSourceSystemId()),
                    lineSepRem(rs.getSourceSystemName()),
                    lineSepRem(rs.getSourceSystemTimezone()),
                    lineSepRem(rs.getSpecificationDescription()),
                    lineSepRem(rs.getSystemFeedFormat()),
                    lineSepRem(rs.getSystemType()),
                    lineSepRem(rs.getLabelId() + ""),
                    lineSepRem(rs.getIsCurrentFlag()),
                    lineSepRem(rs.getStatus()),
                    lineSepRem(rs.getCreatedBy()),
                    lineSepRem(rs.getCreatedDate() + ""),
                    lineSepRem(rs.getUpdatedBy()),
                    lineSepRem(rs.getUpdatedDate() + ""),
                    lineSepRem(rs.getUpdatedReason()),
                    lineSepRem(rs.getPrimaryFlag()),
                    lineSepRem(rs.getActiveFlag()),
                    lineSepRem(rs.getDataOwnerSoeid()),
                    lineSepRem(rs.getLineOfBusiness())
            });

            counter.incrementAndGet();
        });

    } else {
        throw new RuntimeException("Invalid sourceTable: " + sourceTable);
    }

    LOGGER.info("Method out : writeToCsvStream, total records: {}", counter.get());
    return counter.get();
}
