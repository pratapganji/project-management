@Override
public String createConcepts(MultipartFile file, String conceptRequestJson) throws Exception {
    ObjectMapper mapper = new ObjectMapper();

    ConceptRequest conceptRequest =
            mapper.readValue(conceptRequestJson, ConceptRequest.class);

    List<ConceptPayload> conceptPayloadList =
            convertExcelToConceptPayloadList(file);

    LOGGER.info("Concept Request: {}", conceptRequest);
    LOGGER.info("Concept Payload List size: {}", conceptPayloadList.size());

    return mapper.writerWithDefaultPrettyPrinter()
            .writeValueAsString(conceptPayloadList);
}