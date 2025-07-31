package com.citi.olympus.nura.api.scheduler;

import com.citi.olympus.nura.api.constants.AuditTableConstants;
import com.citi.olympus.nura.api.constants.NuraQueryConstants;
import com.citi.olympus.nura.api.constants.PropertyConstants;
import com.citi.olympus.nura.api.service.RestTemplateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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
        ReflectionTestUtils.setField(scheduler, "healthCheckURL", "http://health");
        ReflectionTestUtils.setField(scheduler, "bulkApiRetry", 2);
        scheduler.initRateLimiter();
    }

    @Test
    public void testProcessFailedRequests_successfulRetry() {
        // Mocking input record
        Map<String, Object> record = new HashMap<>();
        record.put(AuditTableConstants.ID, "1");
        record.put(AuditTableConstants.REQUEST_PAYLOAD, "{\"key\":\"value\"}");
        record.put(AuditTableConstants.GLOBAL_TRANSACTION_ID, "txn123");
        record.put(AuditTableConstants.HEADERS, "Content-Type:application/json");

        when(jdbcTemplate.queryForList(eq(NuraQueryConstants.FAILED_AUDIT_QUERY)))
                .thenReturn(Collections.singletonList(record));

        when(jdbcTemplate.queryForObject(eq(NuraQueryConstants.GET_TEMPLATEAPI_HEADERS_QUERY), any(Object[].class), eq(String.class)))
                .thenReturn("{\"ENVIRONMENT\":\"PRODNJ\"}");

        when(propertyConstants.getNyurl()).thenReturn("http://mock-url");

        ResponseEntity<String> mockResponse = new ResponseEntity<>("Success Body", HttpStatus.OK);
        when(restTemplateService.sendRequest(anyString(), eq(HttpMethod.POST), anyString(), anyMap()))
                .thenReturn(mockResponse);

        scheduler.processFailedRequests();

        verify(jdbcTemplate).update(eq(NuraQueryConstants.UPDATE_AUDIT_QUERY_WITH_TIMESTAMP), eq("Success"), eq("Success Body"), eq(1L));
    }

    @Test
    public void testProcessFailedRequests_retryableFailure() {
        Map<String, Object> record = new HashMap<>();
        record.put(AuditTableConstants.ID, "2");
        record.put(AuditTableConstants.REQUEST_PAYLOAD, "{\"key\":\"value\"}");
        record.put(AuditTableConstants.GLOBAL_TRANSACTION_ID, "txn124");
        record.put(AuditTableConstants.HEADERS, "Content-Type:application/json");

        when(jdbcTemplate.queryForList(eq(NuraQueryConstants.FAILED_AUDIT_QUERY)))
                .thenReturn(Collections.singletonList(record));

        when(jdbcTemplate.queryForObject(eq(NuraQueryConstants.GET_TEMPLATEAPI_HEADERS_QUERY), any(Object[].class), eq(String.class)))
                .thenReturn("{\"ENVIRONMENT\":\"PRODNJ\"}");

        when(propertyConstants.getNyurl()).thenReturn("http://mock-url");

        ResponseEntity<String> retryableResponse = new ResponseEntity<>("Retryable", HttpStatus.INTERNAL_SERVER_ERROR);
        when(restTemplateService.sendRequest(anyString(), eq(HttpMethod.POST), anyString(), anyMap()))
                .thenReturn(retryableResponse);

        scheduler.processFailedRequests();

        verify(jdbcTemplate, never()).update(eq(NuraQueryConstants.UPDATE_AUDIT_QUERY_WITH_TIMESTAMP), anyString(), anyString(), anyLong());
    }

    @Test
    public void testProcessFailedRequests_nonRetryableFailure() {
        Map<String, Object> record = new HashMap<>();
        record.put(AuditTableConstants.ID, "3");
        record.put(AuditTableConstants.REQUEST_PAYLOAD, "{\"key\":\"value\"}");
        record.put(AuditTableConstants.GLOBAL_TRANSACTION_ID, "txn125");
        record.put(AuditTableConstants.HEADERS, "Content-Type:application/json");

        when(jdbcTemplate.queryForList(eq(NuraQueryConstants.FAILED_AUDIT_QUERY)))
                .thenReturn(Collections.singletonList(record));

        when(jdbcTemplate.queryForObject(eq(NuraQueryConstants.GET_TEMPLATEAPI_HEADERS_QUERY), any(Object[].class), eq(String.class)))
                .thenReturn("{\"ENVIRONMENT\":\"PRODNJ\"}");

        when(propertyConstants.getNyurl()).thenReturn("http://mock-url");

        ResponseEntity<String> badRequest = new ResponseEntity<>("Bad Request", HttpStatus.BAD_REQUEST);
        when(restTemplateService.sendRequest(anyString(), eq(HttpMethod.POST), anyString(), anyMap()))
                .thenReturn(badRequest);

        scheduler.processFailedRequests();

        verify(jdbcTemplate, never()).update(eq(NuraQueryConstants.UPDATE_AUDIT_QUERY_WITH_TIMESTAMP), anyString(), anyString(), anyLong());
    }

    @Test
    public void testProcessFailedRequests_healthDown() {
        ResponseEntity<String> downResponse = new ResponseEntity<>("Down", HttpStatus.SERVICE_UNAVAILABLE);
        when(restTemplateService.sendRequest(anyString(), any(HttpMethod.class), any(), anyMap()))
                .thenReturn(downResponse);

        ReflectionTestUtils.setField(scheduler, "healthCheckURL", "http://health");
        when(jdbcTemplate.queryForList(NuraQueryConstants.FAILED_AUDIT_QUERY))
                .thenReturn(Collections.emptyList());

        scheduler.processFailedRequests();

        // No DB interaction expected
        verify(jdbcTemplate, never()).update(anyString(), any(), any(), any());
    }
}