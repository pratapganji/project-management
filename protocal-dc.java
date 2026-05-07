

String downstreamResponse = callCollectionApi(requestBody);
return downstreamResponse;



import java.util.List;


private String callCollectionApi(String requestBody) {

    RestTemplate restTemplate = new RestTemplate();

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setAccept(List.of(MediaType.APPLICATION_JSON));

    HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

    ResponseEntity<String> response = restTemplate.exchange(
            collectionUrl,
            HttpMethod.POST,
            entity,
            String.class
    );

    return response.getBody();
}




@Override
public String createConcepts(MultipartFile file, String conceptRequestJson) throws Exception {

    ObjectMapper mapper = new ObjectMapper();

    ConceptRequest conceptRequest =
            mapper.readValue(conceptRequestJson, ConceptRequest.class);

    List<ConceptPayload> conceptPayloadList =
            convertExcelToConceptPayloadList(file);

    LOGGER.info("Concept Request: {}", conceptRequest);
    LOGGER.info("Concept Payload List size: {}", conceptPayloadList.size());

    String requestBody = mapper.writerWithDefaultPrettyPrinter()
            .writeValueAsString(conceptPayloadList);

    LOGGER.info("Prepared request body for collection API");

    // For now return JSON only. Later uncomment when downstream URL is confirmed.
    // String downstreamResponse = callCollectionApi(requestBody);
    // return downstreamResponse;

    return requestBody;
}





@Value("${datacatalogue.collection.url}")
private String collectionUrl;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;




package com.citi.olympus.service;

import org.springframework.web.multipart.MultipartFile;

public interface DataCatalogueService {

    String createConcepts(MultipartFile file, String conceptRequestJson) throws Exception;
}


datacatalogue.collection.url=http://localhost:8080/mock/data-catalogue/collection








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