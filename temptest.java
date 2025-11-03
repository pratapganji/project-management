// ---- CAP verification: print Apigee + Target (WIP) URLs ----
final String APIGEE_BASE = "https://uat.isg.icgservices.citigroup.net/isg/internal/crm/olympus";
final String WIP_BASE   = "https://olympus.api.crmtoolympusnew.uat.cloudgs1.nam.nsroot.net:443"; // note https
final String CLIENT_MEETING_PATH = "/publish/data/clientmeeting";

System.out.println("------------------------------------------------------");
System.out.println("APIGEE Base URL        : " + APIGEE_BASE);
System.out.println("Target Server (WIP) URL: " + WIP_BASE);
System.out.println("Final Apigee Endpoint  : " + APIGEE_BASE + CLIENT_MEETING_PATH);
System.out.println("------------------------------------------------------");

// OPTION A — call APIGEE (recommended)
// Do NOT set baseUrl if you pass an absolute URI below.
WebClient apigeeClient = WebClient.builder()
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
        // simple request/response logger
        .filter((request, next) -> {
            System.out.println("→ REQUEST " + request.method() + " " + request.url());
            return next.exchange(request).doOnNext(resp ->
                System.out.println("← RESPONSE " + resp.statusCode()));
        })
        .build();

Mono<String> resultActionItem = apigeeClient.post()
        .uri(APIGEE_BASE + CLIENT_MEETING_PATH)   // absolute URL → baseUrl not needed
        .bodyValue(clientMeetingbyte)
        .retrieve()
        .bodyToMono(String.class)
        .doOnNext(body -> System.out.println("Response Body: " + body));

System.out.println("Response Action Item: " + resultActionItem.block());
System.out.println("End of WebClient");