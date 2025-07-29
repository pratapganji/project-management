import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.*;
import java.util.stream.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduledProcessor {

    private final JdbcTemplate jdbcTemplate;
    private final RestTemplate restTemplate;
    private final RateLimiterRegistry rateLimiterRegistry;

    @Value("${external.api.healthCheckUrl}")
    private String healthCheckUrl;

    @Scheduled(fixedDelayString = "${scheduler.fixedDelay:30000}")
    public void processFailedRequests() {
        List<RequestPayload> requests = jdbcTemplate.query(
                "SELECT id, payload, api_endpoint, headers FROM requests WHERE status = 'FAILED'",
                requestRowMapper
        );

        if (requests.isEmpty()) {
            log.info("No failed records found.");
            return;
        }

        log.info("Found {} failed records", requests.size());

        if (!isHealthCheckUp()) {
            log.warn("Health check failed. Skipping processing.");
            return;
        }

        RateLimiter rateLimiter = rateLimiterRegistry.rateLimiter("apiLimiter");

        for (RequestPayload payload : requests) {
            Runnable task = RateLimiter.decorateRunnable(rateLimiter, () -> sendSingleRequest(payload));
            try {
                task.run();
            } catch (RequestNotPermitted e) {
                log.warn("Rate limit exceeded for record {}. Skipping.", payload.getId());
            }
        }
    }

    private boolean isHealthCheckUp() {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(healthCheckUrl, String.class);
            boolean isUp = response.getStatusCode() == HttpStatus.OK;
            log.info("Health check status: {}", isUp ? "UP" : "DOWN");
            return isUp;
        } catch (Exception e) {
            log.error("Health check failed: {}", e.getMessage());
            return false;
        }
    }

    private void sendSingleRequest(RequestPayload payload) {
        try {
            HttpHeaders headers = parseHeaders(payload.getHeaders());
            HttpEntity<String> entity = new HttpEntity<>(payload.getPayload(), headers);

            log.info("Sending request to: {}", payload.getApiEndpoint());

            ResponseEntity<String> response = restTemplate.exchange(
                    payload.getApiEndpoint(),
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                jdbcTemplate.update("UPDATE requests SET status = 'SUCCESS' WHERE id = ?", payload.getId());
                log.info("Record {} processed successfully.", payload.getId());
            } else {
                log.warn("Record {} failed with response status: {}", payload.getId(), response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Exception while processing record {}: {}", payload.getId(), e.getMessage());
        }
    }

    private HttpHeaders parseHeaders(String headersStr) {
        HttpHeaders headers = new HttpHeaders();
        if (headersStr == null || headersStr.trim().isEmpty()) return headers;

        Arrays.stream(headersStr.replaceAll("[{}]", "").split(","))
                .map(s -> s.split("=", 2))
                .filter(arr -> arr.length == 2)
                .forEach(arr -> headers.add(arr[0].trim(), arr[1].trim()));

        return headers;
    }

    private final RowMapper<RequestPayload> requestRowMapper = (rs, rowNum) -> {
        RequestPayload payload = new RequestPayload();
        payload.setId(rs.getLong("id"));
        payload.setPayload(rs.getString("payload"));
        payload.setApiEndpoint(rs.getString("api_endpoint"));
        payload.setHeaders(rs.getString("headers"));
        return payload;
    };
}
----------------------------------




@Component
@ConfigurationProperties(prefix = "bulkapiconfig")
public class BulkApiConfig {

    private String url;
    private String healthCheckUrl;
    private String dataSource;
    private double rateLimit;
    private long scheduleFixedRate;

    // Getters and Setters
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getHealthCheckUrl() {
        return healthCheckUrl;
    }

    public void setHealthCheckUrl(String healthCheckUrl) {
        this.healthCheckUrl = healthCheckUrl;
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public double getRateLimit() {
        return rateLimit;
    }

    public void setRateLimit(double rateLimit) {
        this.rateLimit = rateLimit;
    }

    public long getScheduleFixedRate() {
        return scheduleFixedRate;
    }

    public void setScheduleFixedRate(long scheduleFixedRate) {
        this.scheduleFixedRate = scheduleFixedRate;
    }
}
---------------------------------------------------




package com.citi.olympus.nura.api.scheduler;

import com.citi.olympus.nura.api.service.RestTemplateService;
import com.citi.olympus.nura.utility.config.BulkApiConfig;
import com.google.common.util.concurrent.RateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class BulkRetryScheduler {

    private static final Logger LOG = LoggerFactory.getLogger(BulkRetryScheduler.class);
    private static final String FAILED_AUDIT_QUERY = "SELECT * FROM OM_NURAFLOW_AUDIT_DATA WHERE STATUS = 'FAILED'";
    private static final String UPDATE_AUDIT_QUERY = "UPDATE OM_NURAFLOW_AUDIT_DATA SET STATUS = ?, RESPONSE_PAYLOAD = ? WHERE ID = ?";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private RestTemplateService restTemplateService;

    @Autowired
    private BulkApiConfig bulkApiConfig;

    private RateLimiter rateLimiter;

    @PostConstruct
    public void initRateLimiter() {
        this.rateLimiter = RateLimiter.create(bulkApiConfig.getRateLimit());
        LOG.info("RateLimiter initialized with rate = {} requests/second", bulkApiConfig.getRateLimit());
    }

    @Scheduled(fixedRateString = "#{@bulkApiConfig.scheduleFixedRate}")
    public void processFailedRequests() {
        try {
            List<Map<String, Object>> failedRecords = jdbcTemplate.queryForList(FAILED_AUDIT_QUERY);

            for (Map<String, Object> row : failedRecords) {
                ResponseEntity<String> healthResponse = new org.springframework.web.client.RestTemplate()
                        .getForEntity(bulkApiConfig.getHealthCheckUrl(), String.class);

                if (healthResponse.getStatusCode() == HttpStatus.OK) {
                    LOG.info("Bulk API status is UP");

                    Long id = ((Number) row.get("ID")).longValue();
                    String payload = (String) row.get("REQUEST_PAYLOAD");
                    String headers = (String) row.get("HEADERS");

                    Map<String, String> headersMap = Arrays.stream(headers.replaceAll("[{}]", "").split(",", 2))
                            .map(s -> s.split("=", 2))
                            .filter(arr -> arr.length == 2)
                            .collect(Collectors.toMap(
                                    arr -> arr[0].trim(),
                                    arr -> arr[1].trim()
                            ));

                    rateLimiter.acquire();
                    LOG.info("Permit acquired. Sending request...");

                    ResponseEntity<String> response = restTemplateService.sendRequest(
                            bulkApiConfig.getUrl(), HttpMethod.POST, payload, headersMap
                    );

                    if (response.getStatusCode() == HttpStatus.OK) {
                        updateStatus(id, "SUCCESS", response.getBody());
                        LOG.info("Updated record {} as SUCCESS", id);
                    } else {
                        LOG.error("Record {} failed with response: {}", id, response.getStatusCode());
                    }
                } else {
                    LOG.info("Health check failed. Skipping processing.");
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void updateStatus(Long id, String newStatus, String responseBody) {
        jdbcTemplate.update(UPDATE_AUDIT_QUERY, newStatus, responseBody, id);
    }
}




-----------------------------------

bulkapiconfig:
  url: https://olympus.api.bulk.uat.cloudqsl.nam.nsroot.net/v2/olympus/bulk
  healthCheckUrl: https://olympus.api.bulk.uat.cloudqsl.nam.nsroot.net/olympus/service/health
  rateLimit: 2.0
  scheduleFixedRate: 30000
