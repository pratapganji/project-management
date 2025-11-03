// ---- Base URLs ----
final String WIP_BASE = "https://olympus.api.crmtoolympusnew.uat.cloudgs1.nam.nsroot.net:443";
final String APIGEE_BASE = "https://uat.isg.icgservices.citigroup.net/isg/internal/crm/olympus";

// ---- Endpoints & Body Mapping ----
Map<String, Object> endpoints = new HashMap<>();
endpoints.put("/publish/data/clientmeeting", clientMeetingbyte);
endpoints.put("/publish/data/callreport", callReportbyte);
endpoints.put("/publish/data/clientactionitem", actionItembyte);
endpoints.put("/publish/details/client/productivitymetrics", productivityMetricsbyte);

// ---- Create WebClient ----
WebClient webClient = WebClient.builder()
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
        .filter((request, next) -> {
            System.out.println("→ REQUEST " + request.method() + " " + request.url());
            return next.exchange(request).doOnNext(resp ->
                    System.out.println("← RESPONSE " + resp.statusCode()));
        })
        .build();

// ---- Loop through all endpoints ----
for (Map.Entry<String, Object> entry : endpoints.entrySet()) {
    String path = entry.getKey();
    Object body = entry.getValue();

    // Skip null bodies (if any endpoint byte array is not initialized)
    if (body == null) {
        System.out.println("Skipping endpoint " + path + " (no payload)");
        continue;
    }

    // Print CAP verification info for each endpoint
    System.out.println("=====================================================");
    System.out.println("CAP VERIFICATION (UAT) - APIGEE & TARGET (WIP) HTTPS");
    System.out.println("APIGEE Base URL        : " + APIGEE_BASE);
    System.out.println("Target Server (WIP) URL: " + WIP_BASE);
    System.out.println("Final Endpoint (Apigee): " + APIGEE_BASE + path);
    System.out.println("=====================================================");

    // Send the request
    Mono<String> responseMono = webClient.post()
            .uri(APIGEE_BASE + path)
            .bodyValue(body)
            .retrieve()
            .bodyToMono(String.class)
            .doOnNext(respBody -> System.out.println("Response Body: " + respBody));

    // Block sequentially for each call
    String response = responseMono.block();
    System.out.println("Response for " + path + ": " + response);
    System.out.println("End of WebClient for " + path);
    System.out.println();
}
