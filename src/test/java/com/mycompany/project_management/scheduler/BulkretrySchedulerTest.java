package com.mycompany.project_management.scheduler;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.concurrent.TimeUnit;
import com.google.common.util.concurrent.RateLimiter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class BulkretrySchedulerTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private RestTemplateService restTemplateService;

    @Mock
    private PropertyConstants propertyConstants;

    @InjectMocks
    private BulkRetryScheduler bulkRetryScheduler;

    private final String HEALTH_URL = "http://health-check";

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(bulkRetryScheduler, "rateLimit", 10.0);
        ReflectionTestUtils.setField(bulkRetryScheduler, "burstTime", 5L);
        ReflectionTestUtils.setField(bulkRetryScheduler, "healthCheckUrl", HEALTH_URL);
        ReflectionTestUtils.setField(bulkRetryScheduler, "maxRetries", 2);
        ReflectionTestUtils.setField(bulkRetryScheduler, "backoffMillis", 10L); // lower for test speed

        bulkRetryScheduler.initRateLimiter();
    }

    @Test
    public void testProcessFailedRequests_whenHealthCheckIsUp_andRequestSucceeds() {
        // Arrange
        List<Map<String, Object>> failedRecords = new ArrayList<>();
        Map<String, Object> record = new HashMap<>();
        record.put("ID", "123");
        record.put("REQUEST_PAYLOAD", "{\"data\":\"test\"}");
        record.put("GLOBAL_TRANSACTION_ID", "txn-1");
        record.put("HEADERS", "{Authorization: Bearer token}");
        failedRecords.add(record);

        when(jdbcTemplate.queryForList(anyString())).thenReturn(failedRecords);
        when(jdbcTemplate.queryForObject(anyString(), any(Object[].class), eq(String.class)))
                .thenReturn("{ENVIRONMENT:PRODNY}");
        when(propertyConstants.getNyurl()).thenReturn("http://ny-service");
        when(propertyConstants.getUrl()).thenReturn("http://default-url");

        ResponseEntity<String> healthOk = new ResponseEntity<>("UP", HttpStatus.OK);
        ReflectionTestUtils.setField(bulkRetryScheduler, "restTemplate", mock(org.springframework.web.client.RestTemplate.class));
        when(((org.springframework.web.client.RestTemplate) ReflectionTestUtils.getField(bulkRetryScheduler, "restTemplate"))
                .getForEntity(eq(HEALTH_URL), eq(String.class))).thenReturn(healthOk);

        ResponseEntity<String> successResponse = new ResponseEntity<>("Success", HttpStatus.OK);
        when(restTemplateService.sendRequest(anyString(), eq(HttpMethod.POST), anyString(), anyMap()))
                .thenReturn(successResponse);

        // Act
        bulkRetryScheduler.processFailedRequests();

        // Assert
        verify(jdbcTemplate).update(anyString(), eq("Success"), eq("Success"), eq(123L));
    }

    @Test
    public void testProcessFailedRequests_whenHealthCheckFails() {
        // Arrange
        ResponseEntity<String> healthFail = new ResponseEntity<>("DOWN", HttpStatus.INTERNAL_SERVER_ERROR);
        ReflectionTestUtils.setField(bulkRetryScheduler, "restTemplate", mock(org.springframework.web.client.RestTemplate.class));
        when(((org.springframework.web.client.RestTemplate) ReflectionTestUtils.getField(bulkRetryScheduler, "restTemplate"))
                .getForEntity(eq(HEALTH_URL), eq(String.class))).thenReturn(healthFail);

        // Act
        bulkRetryScheduler.processFailedRequests();

        // Assert
        verify(jdbcTemplate, never()).queryForList(anyString());
    }
}

