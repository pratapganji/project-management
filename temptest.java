package com.citi.olympus.nura.api.scheduler;

import com.citi.olympus.nura.api.constants.AuditTableConstants;
import com.citi.olympus.nura.api.constants.NuraQueryConstants;
import com.citi.olympus.nura.api.constants.PropertyConstants;
import com.citi.olympus.nura.api.service.RestTemplateService;
import com.google.common.util.concurrent.RateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.citi.olympus.nura.api.constants.NuraQueryConstants.PRODNJ;
import static com.citi.olympus.nura.api.constants.NuraQueryConstants.PRODNY;

@Component
public class BulkRetryScheduler {

    private static final Logger LOG = LoggerFactory.getLogger(BulkRetryScheduler.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private RestTemplateService restTemplateService;

    @Autowired
    private PropertyConstants propertyConstants;

    @Value("${bulkapiconfig.rateLimit}")
    private double rateLimit;

    @Value("${bulkapiconfig.burstTime}")
    private long burstTime;

    @Value("${bulkapiconfig.healthCheckUrl}")
    private String healthCheckUrl;

    @Value("${bulkapiconfig.bulkApiRetry}")
    private int maxRetries;

    private RateLimiter rateLimiter;

    private final RestTemplate restTemplate = new RestTemplate();

    long backoffMillis = 1000; // 1 second

    @PostConstruct
    public void initRateLimiter() {
        this.rateLimiter = RateLimiter.create(rateLimit, burstTime, TimeUnit.SECONDS);
        LOG.info("RateLimiter initialized with rate = " + rateLimit + " requests/second with burst time " + burstTime);
    }

    @Scheduled(fixedRateString = "${bulkapiconfig.scheduleFixedRate}")
    public void processFailedRequests() {
        LOG.info("Starting Bulk Retry Scheduler...");

        ResponseEntity<String> healthResponse = restTemplate.getForEntity(healthCheckUrl, String.class);

        if (healthResponse.getStatusCode() == HttpStatus.OK) {
            LOG.info("Bulk API Status is UP");

            List<Map<String, Object>> failedRecords = jdbcTemplate.queryForList(NuraQueryConstants.FAILED_AUDIT_QUERY);
            LOG.info("Number of failed records to process: " + failedRecords.size());

            for (Map<String, Object> row : failedRecords) {
                try {
                    String id = (String) row.get(AuditTableConstants.ID);
                    String requestPayload = (String) row.get(AuditTableConstants.REQUEST_PAYLOAD);
                    String globalTransactionId = (String) row.get(AuditTableConstants.GLOBAL_TRANSACTION_ID);
                    String headers = (String) row.get(AuditTableConstants.HEADERS);

                    Map<String, String> headersMap = processHeaders(headers);

                    String templateApiHeaders = jdbcTemplate.queryForObject(
                            NuraQueryConstants.GET_TEMPLATEAPI_HEADERS_QUERY,
                            new Object[]{globalTransactionId},
                            String.class
                    );

                    String environment = getJsonElement(templateApiHeaders, AuditTableConstants.ENVIRONMENT);

                    String serviceURL = propertyConstants.getUrl();
                    if (environment != null && PRODNJ.equalsIgnoreCase(environment)) {
                        serviceURL = propertyConstants.getNjurl();
                    } else if (environment != null && PRODNY.equalsIgnoreCase(environment)) {
                        serviceURL = propertyConstants.getNyurl();
                    }

                    rateLimiter.acquire();
                    LOG.info("Permit acquired");

                    int attempt = 0;
                    ResponseEntity<String> response = null;

                    while (attempt < maxRetries) {
                        attempt++;
                        try {
                            response = restTemplateService.sendRequest(serviceURL, HttpMethod.POST, requestPayload, headersMap);

                            if (response.getStatusCode() == HttpStatus.OK) {
                                updateStatusWithTimestamp(Long.parseLong(id), "Success", response.getBody());
                                LOG.info("Updated record " + id + " as SUCCESS");
                                break;
                            } else if (response.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR ||
                                       response.getStatusCode() == HttpStatus.NOT_FOUND) {
                                LOG.warn("Retryable error occurred: " + response.getStatusCode() + ". Retrying...");
                            } else {
                                LOG.error("Non-retryable error for record " + id + " with status: " + response.getStatusCode());
                                break;
                            }
                        } catch (Exception ex) {
                            LOG.error("Attempt " + attempt + ": Exception while sending request for record " + id, ex);
                        }

                        if (attempt < maxRetries) {
                            try {
                                Thread.sleep(backoffMillis);
                            } catch (InterruptedException ie) {
                                Thread.currentThread().interrupt();
                                break;
                            }
                        } else {
                            LOG.error("All retry attempts failed for record " + id);
                            updateStatus(Long.parseLong(id), "Failed", response != null ? response.getBody() : null);
                        }
                    }

                } catch (Exception ex) {
                    LOG.error("Error processing record: ", ex);
                }
            }
        } else {
            LOG.error("Bulk API Status is DOWN. Skipping retry processing.");
        }
    }

    private String getJsonElement(String request, String key) {
        Map<String, String> result = Arrays.stream(request.replaceAll("[{}]", "")
                        .split(","))
                .map(s -> s.split(":", 2))
                .filter(arr -> arr.length == 2)
                .collect(Collectors.toMap(
                        arr -> arr[0].trim(),
                        arr -> arr[1].trim()
                ));
        return result.get(key);
    }

    private void updateStatus(Long id, String newStatus, String responseBody) {
        jdbcTemplate.update(NuraQueryConstants.UPDATE_AUDIT_QUERY_WITH_TIMESTAMP, newStatus, responseBody, id);
    }

    private void updateStatusWithTimestamp(Long id, String newStatus, String responseBody) {
        jdbcTemplate.update(NuraQueryConstants.UPDATE_AUDIT_QUERY_WITH_TIMESTAMP, newStatus, responseBody, id);
    }

    private Map<String, String> processHeaders(String headers) {
        return Arrays.stream(headers.replaceAll("[{}]", "")
                        .split(","))
                .map(s -> s.split(":", 2))
                .filter(arr -> arr.length == 2)
                .collect(Collectors.toMap(
                        arr -> arr[0].trim(),
                        arr -> arr[1].trim()
                ));
    }
}