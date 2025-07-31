package com.citi.olympus.nura.api.scheduler;

import com.citi.olympus.nura.api.constants.AuditTableConstants;
import com.citi.olympus.nura.api.constants.NuraQueryConstants;
import com.citi.olympus.nura.api.constants.PropertyConstants;
import com.citi.olympus.nura.api.service.RestTemplateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BulkRetrySchedulerTest {

    @InjectMocks
    private BulkRetryScheduler scheduler;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private RestTemplateService restTemplateService;

    @Mock
    private PropertyConstants propertyConstants;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(scheduler, "maxRetries", 2);
        ReflectionTestUtils.setField(scheduler, "rateLimit", 5.0);
        ReflectionTestUtils.setField(scheduler, "burstTime", 1L);
        ReflectionTestUtils.setField(scheduler, "healthCheckURL", "http://mock-health-url.com");
        scheduler.initRateLimiter();
    }

    @Test
    public void testProcessFailedRequests_successfulRetry_PRODNJ() {
        mockEnvironment("PRODNJ", "http://mock-nj-url");

        Map<String, Object> record = createRecord("1", "txn123", "{\"key\":\"value\"}");
        mockDBAndRequest(record, HttpStatus.OK, "Success Body");

        scheduler.processFailedRequests();

        verify(jdbcTemplate).update(eq(NuraQueryConstants.UPDATE_AUDIT_QUERY_WITH_TIMESTAMP),
                eq("Success"), eq("Success Body"), eq(1L));
    }

    @Test
    public void testProcessFailedRequests_successfulRetry_PRODNY() {
        mockEnvironment("PRODNY", "http://mock-ny-url");

        Map<String, Object> record = createRecord("10", "txn321", "{\"k\":\"v\"}");
        mockDBAndRequest(record, HttpStatus.OK, "Success");

        scheduler.processFailedRequests();

        verify(jdbcTemplate).update(eq(NuraQueryConstants.UPDATE_AUDIT_QUERY_WITH_TIMESTAMP),
                eq("Success"), eq("Success"), eq(1L));
    }

    @Test
    public void testProcessFailedRequests_successfulRetry_defaultEnv() {
        mockEnvironment("UAT", "http://default-url");

        Map<String, Object> record = createRecord("100", "txn999", "{\"x\":\"z\"}");
        mockDBAndRequest(record, HttpStatus.OK, "Success");

        scheduler.processFailedRequests();

        verify(jdbcTemplate).update(eq(NuraQueryConstants.UPDATE_AUDIT_QUERY_WITH_TIMESTAMP),
                eq("Success"), eq("Success"), eq(1L));
    }

    @Test
    public void testProcessFailedRequests_retryableFailure() {
        mockEnvironment("PRODNJ", "http://mock-nj-url");

        Map<String, Object> record = createRecord("2", "txn124", "{\"key\":\"value\"}");
        mockDBAndRequest(record, HttpStatus.INTERNAL_SERVER_ERROR, "Retryable");

        scheduler.processFailedRequests();

        verify(jdbcTemplate, never()).update(anyString(), anyString(), anyString(), anyLong());
    }

    @Test
    public void testProcessFailedRequests_nonRetryableFailure() {
        mockEnvironment("PRODNJ", "http://mock-nj-url");

        Map<String, Object> record = createRecord("3", "txn125", "{\"key\":\"value\"}");
        mockDBAndRequest(record, HttpStatus.BAD_REQUEST, "Bad Request");

        scheduler.processFailedRequests();

        verify(jdbcTemplate, never()).update(anyString(), anyString(), anyString(), anyLong());
    }

    @Test
    public void testProcessFailedRequests_healthCheckDown() {
        ResponseEntity<String> healthDown = new ResponseEntity<>("Down", HttpStatus.SERVICE_UNAVAILABLE);
        when(restTemplateService.sendRequest(anyString(), eq(HttpMethod.GET), anyMap())).thenReturn(healthDown);

        ReflectionTestUtils.setField(scheduler, "healthCheckURL", "http://health");

        scheduler.processFailedRequests();

        verify(jdbcTemplate, never()).update(anyString(), any(), any(), any());
    }

    private void mockEnvironment(String env, String url) {
        when(jdbcTemplate.queryForList(eq(NuraQueryConstants.FAILED_AUDIT_QUERY)))
                .thenReturn(Collections.singletonList(createRecord("1", "txn", "{\"key\":\"v\"}")));

        when(jdbcTemplate.queryForObject(eq(NuraQueryConstants.GET_TEMPLATEAPI_HEADERS_QUERY),
                any(Object[].class), eq(String.class)))
                .thenReturn("{\"ENVIRONMENT\":\"" + env + "\"}");

        if ("PRODNJ".equalsIgnoreCase(env)) {
    when(propertyConstants.getNjurl()).thenReturn(url);
} else if ("PRODNY".equalsIgnoreCase(env)) {
    when(propertyConstants.getNyurl()).thenReturn(url);
} else {
    when(propertyConstants.getUrl()).thenReturn(url);
}
    }

    private void mockDBAndRequest(Map<String, Object> record, HttpStatus status, String responseBody) {
        when(jdbcTemplate.queryForList(eq(NuraQueryConstants.FAILED_AUDIT_QUERY)))
                .thenReturn(Collections.singletonList(record));

        when(jdbcTemplate.queryForObject(eq(NuraQueryConstants.GET_TEMPLATEAPI_HEADERS_QUERY),
                any(Object[].class), eq(String.class)))
                .thenReturn("{\"ENVIRONMENT\":\"" + record.get("ENVIRONMENT") + "\"}");

        when(restTemplateService.sendRequest(anyString(), eq(HttpMethod.POST), anyMap()))
                .thenReturn(new ResponseEntity<>(responseBody, status));
    }

    private Map<String, Object> createRecord(String id, String txnId, String payload) {
        Map<String, Object> record = new HashMap<>();
        record.put(AuditTableConstants.ID, id);
        record.put(AuditTableConstants.GLOBAL_TRANSACTION_ID, txnId);
        record.put(AuditTableConstants.REQUEST_PAYLOAD, payload);
        record.put(AuditTableConstants.HEADERS, "Content-Type: application/json");
        return record;
    }
}