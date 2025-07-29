package com.mycompany.project_management.scheduler;

import com.mycompany.project_management.constants.NuraSqlConstants;

import java.util.concurrent.TimeUnit;

@Component
public class BulkRetryScheduler1 {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private RestTemplateService restTemplateService;

    @Value("${bulkapiconfig.rateLimit}")
    private double rateLimit;

    @Value("${bulkapiconfig.burstTime}")
    private double burstTime;

    private RateLimiter rateLimiter;

    private static final Logger LOG = LoggerFactory.getLogger(BulkRetryScheduler.class);

    private String HEALTH_CHECK_URL = "https://olympus.api.bulk.uat.cloudgsl.nam.nsroot.net/olympus/service/health";

    private final RestTemplate restTemplate = new RestTemplate();

    @PostConstruct
    public void initRateLimiter() {
        this.rateLimiter = RateLimiter.create(rateLimit, burstTime, TimeUnit.SECONDS);
        LOG.info("RateLimiter initialized with rate = " + rateLimit + " requests/second" + "  with burst time "+burstTime);
    }

    @Scheduled(fixedRate = "${bulkapiconfig.scheduleTime}") // every 30 seconds
    public void processFailedRequests() {

        List<Map<String, Object>> failedRecords = jdbcTemplate.queryForList(NuraSqlConstants.AUDIT_BULK_API_FAILED_SQL);

        for (Map<String, Object> row : failedRecords) {
            try {
                ResponseEntity<String> healthResponse = restTemplate.getForEntity(HEALTH_CHECK_URL, String.class);
                if (healthResponse.getStatusCode() == HttpStatus.OK) {
                    LOG.info("Bulk API Status is UP");

                    Long id = ((Number) row.get("ID")).longValue();
                    String payload = (String) row.get("REQUEST_PAYLOAD");
                    String url = (String) row.get("API_ENDPOINT");
                    String headers = (String) row.get("HEADERS");

                    Map<String, String> headersMap = Arrays.stream(headers.replaceAll("[{}]", "").split(","))
                        .map(s -> s.split("=", 2))
                        .filter(arr -> arr.length == 2)
                        .collect(Collectors.toMap(
                            arr -> arr[0].trim(),
                            arr -> arr[1].trim()
                        ));

                    rateLimiter.acquire();
                    LOG.info("Permit acquired. Sending request...");

                    ResponseEntity<String> response = restTemplateService.sendRequest(
                        url, HttpMethod.POST, payload, headersMap);

                    if (response.getStatusCode() == HttpStatus.OK) {
                        updateStatus(id, "SUCCESS", response.getBody());
                        LOG.info("Updated record " + id + " as SUCCESS");
                    } else {
                        LOG.error("Record " + id + " failed with response: " + response.getStatusCode());
                    }
                } else {
                    LOG.info("Health check failed. Skipping processing.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void updateStatus(Long id, String newStatus, String responseBody) {
        jdbcTemplate.update(NuraSqlConstants.AUDIT_BULK_API_UPDATE_SQL, newStatus, responseBody, id);
    }
}