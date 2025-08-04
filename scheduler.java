@Component
public class BulkRetryScheduler {

    private static final Logger LOG = LoggerFactory.getLogger(BulkRetryScheduler.class);

    @Autowired
    @Qualifier("oracleJdbcTemplate")
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private RestTemplateService restTemplateService;

    @Autowired
    private PropertyConstants propertyConstants;

    @Autowired
    private RetryTemplate retryTemplate;

    @Value("${bulkapiconfig.rateLimit}")
    private double rateLimit;

    @Value("${bulkapiconfig.burstTime}")
    private long burstTime;

    @Value("${bulkapiconfig.healthCheckUrl}")
    private String healthCheckUrl;

    private RateLimiter rateLimiter;

    private final RestTemplate restTemplate = new RestTemplate();

    @PostConstruct
    public void initRateLimiter() {
        this.rateLimiter = RateLimiter.create(rateLimit, burstTime, TimeUnit.SECONDS);
        LOG.info("RateLimiter initialized with rate = " + rateLimit + " requests/second" + " with burst time " + burstTime);
    }

    @Scheduled(fixedRateString = "${bulkapiconfig.schedulerFixedRate}") // every 1 hour
    public void processFailedRequests() {
        LOG.info("Starting Bulk Retry Scheduler...");

        ResponseEntity<String> healthResponse = restTemplate.getForEntity(healthCheckUrl, String.class);

        if (healthResponse.getStatusCode() == HttpStatus.OK) {
            LOG.info("Bulk API Status is UP");

            List<Map<String, Object>> failedRecords = jdbcTemplate.queryForList(NuraQueryConstants.FAILED_AUDIT_QUERY);

            LOG.info("Number of failed records to process = " + failedRecords.size());

            for (Map<String, Object> row : failedRecords) {
                try {
                    Long id = ((Number) row.get(AuditTableConstants.ID)).longValue();
                    String requestPayload = (String) row.get(AuditTableConstants.REQUEST_PAYLOAD);
                    String globalTransactionId = (String) row.get(AuditTableConstants.GLOBAL_TRANSACTION_ID);
                    String headers = (String) row.get(AuditTableConstants.HEADERS);

                    Map<String, String> headersMap = processHeaders(headers);

                    String templateApiHeaders = jdbcTemplate.queryForObject(
                        NuraQueryConstants.GET_TEMPLATEAPI_HEADERS_QUERY,
                        new Object[]{globalTransactionId}, String.class
                    );

                    String environment = getJsonElement(templateApiHeaders, AuditTableConstants.ENVIRONMENT);

                    String serviceURL = propertyConstants.getUrl();
                    if (null != environment && PRODNY.equalsIgnoreCase(environment)) {
                        serviceURL = propertyConstants.getNyurl();
                    }
                    if (null != environment && PRODNJ.equalsIgnoreCase(environment)) {
                        serviceURL = propertyConstants.getNjurl();
                    }

                    rateLimiter.acquire();
                    LOG.info("Permit acquired");

                    String finalServiceURL = serviceURL;
                    retryTemplate.execute((RetryContext context) -> {
                        if (context.getRetryCount() > 0) {
                            LOG.info("Retry attempt: {}", context.getRetryCount());
                        }

                        ResponseEntity<String> response = restTemplateService.sendRequest(finalServiceURL, HttpMethod.POST, requestPayload, headersMap);

                        if (response.getStatusCode().is2xxSuccessful()) {
                            LOG.info("Success sending request to Bulk API. Response Body: {}", response.getBody());
                            updateStatusWithTimestamp(id, AuditTableConstants.SUCCESS, response.getBody());
                        } else if (response.getStatusCode().is4xxClientError()) {
                            LOG.error("Client error during Bulk API request: {}", response.getStatusCode());
                        } else if (response.getStatusCode().is5xxServerError()) {
                            LOG.warn("Server error during Bulk API request: {}", response.getStatusCode());
                        } else {
                            LOG.warn("Unexpected status code during Bulk API request: {}", response.getStatusCode());
                            updateStatusWithTimestamp(id, AuditTableConstants.FAILED, (response != null) ? response.getBody() : null);
                        }

                        return response;
                    });

                } catch (Exception ex) {
                    LOG.error("Error processing record: ", ex);
                }
            }
        }
    }

    private String getJsonElement(String request, String key) {
        Map<String, String> result = Arrays.stream(request.replaceAll("[{}\"]", "").split(","))
            .map(s -> s.split(":", 2))
            .filter(arr -> arr.length == 2)
            .collect(Collectors.toMap(
                arr -> arr[0].trim(),
                arr -> arr[1].trim()
            ));
        return result.get(key);
    }

    private Map<String, String> processHeaders(String headers) {
        return Arrays.stream(headers.replaceAll("[{}]", "").split(","))
            .map(s -> s.split("=", 2))
            .filter(arr -> arr.length == 2)
            .collect(Collectors.toMap(
                arr -> arr[0].trim(),
                arr -> arr[1].trim()
            ));
    }

    private void updateStatusWithTimestamp(Long id, String newStatus, String responseBody) {
        jdbcTemplate.update(NuraQueryConstants.UPDATE_AUDIT_QUERY_WITH_TIMESTAMP, newStatus, responseBody, id);
    }
}