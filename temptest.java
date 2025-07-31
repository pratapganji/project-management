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

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class BulkRetrySchedulerTest {

    @InjectMocks
    private BulkRetryScheduler scheduler;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private RestTemplateService restTemplateService;

    @Mock
    private PropertyConstants propertyConstants;

    @Mock
    private ResponseEntity<String> mockResponse;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(scheduler, "healthCheckUrl", "http://localhost/health");
        ReflectionTestUtils.setField(scheduler, "rateLimit", 5.0);
        ReflectionTestUtils.setField(scheduler, "burstTime", 5L);
        ReflectionTestUtils.setField(scheduler, "maxRetries", 2);
        scheduler.initRateLimiter();
    }

    @Test
    public void testProcessFailedRequests_successfulRetry() {
        Map<String, Object> record = createTestRecord("1", "{\"key\":\"value\"}", "GTID", "key:value");
        when(jdbcTemplate.queryForList(NuraQueryConstants.FAILED_AUDIT_QUERY)).thenReturn(List.of(record));
        when(jdbcTemplate.queryForObject(eq(NuraQueryConstants.GET_TEMPLATEAPI_HEADERS_QUERY), any(Object[].class), eq(String.class)))
                .thenReturn("{\"ENVIRONMENT\":\"PRODN\"}");
        when(propertyConstants.getNyurl()).thenReturn("http://bulk-api");
        when(restTemplateService.sendRequest(anyString(), eq(HttpMethod.POST), anyString(), anyMap()))
                .thenReturn(new ResponseEntity<>("Success Body", HttpStatus.OK));
        when(jdbcTemplate.update(eq(NuraQueryConstants.UPDATE_AUDIT_QUERY_WITH_TIMESTAMP), eq("Success"), eq("Success Body"), eq(1L)))
                .thenReturn(1);

        when(mockResponse.getStatusCode()).thenReturn(HttpStatus.OK);
        when(mockResponse.getBody()).thenReturn("UP");

        ResponseEntity<String> healthCheck = new ResponseEntity<>("UP", HttpStatus.OK);
        when(scheduler.restTemplate.getForEntity(anyString(), eq(String.class))).thenReturn(healthCheck);

        scheduler.processFailedRequests();

        verify(jdbcTemplate).update(eq(NuraQueryConstants.UPDATE_AUDIT_QUERY_WITH_TIMESTAMP), eq("Success"), eq("Success Body"), eq(1L));
    }

    @Test
    public void testProcessFailedRequests_retryableErrorThenSuccess() {
        Map<String, Object> record = createTestRecord("1", "{\"key\":\"value\"}", "GTID", "key:value");
        when(jdbcTemplate.queryForList(NuraQueryConstants.FAILED_AUDIT_QUERY)).thenReturn(List.of(record));
        when(jdbcTemplate.queryForObject(anyString(), any(Object[].class), eq(String.class)))
                .thenReturn("{\"ENVIRONMENT\":\"PRODN\"}");
        when(propertyConstants.getNyurl()).thenReturn("http://bulk-api");

        when(restTemplateService.sendRequest(anyString(), any(), anyString(), anyMap()))
                .thenReturn(new ResponseEntity<>("Fail", HttpStatus.INTERNAL_SERVER_ERROR))
                .thenReturn(new ResponseEntity<>("Success Body", HttpStatus.OK));

        ResponseEntity<String> healthCheck = new ResponseEntity<>("UP", HttpStatus.OK);
        when(scheduler.restTemplate.getForEntity(anyString(), eq(String.class))).thenReturn(healthCheck);

        scheduler.processFailedRequests();

        verify(jdbcTemplate).update(eq(NuraQueryConstants.UPDATE_AUDIT_QUERY_WITH_TIMESTAMP), eq("Success"), eq("Success Body"), eq(1L));
    }

    @Test
    public void testProcessFailedRequests_nonRetryableError() {
        Map<String, Object> record = createTestRecord("2", "{\"key\":\"value\"}", "GTID", "key:value");
        when(jdbcTemplate.queryForList(NuraQueryConstants.FAILED_AUDIT_QUERY)).thenReturn(List.of(record));
        when(jdbcTemplate.queryForObject(anyString(), any(Object[].class), eq(String.class)))
                .thenReturn("{\"ENVIRONMENT\":\"PRODN\"}");
        when(propertyConstants.getNyurl()).thenReturn("http://bulk-api");

        when(restTemplateService.sendRequest(anyString(), any(), anyString(), anyMap()))
                .thenReturn(new ResponseEntity<>("Unauthorized", HttpStatus.UNAUTHORIZED));

        ResponseEntity<String> healthCheck = new ResponseEntity<>("UP", HttpStatus.OK);
        when(scheduler.restTemplate.getForEntity(anyString(), eq(String.class))).thenReturn(healthCheck);

        scheduler.processFailedRequests();

        verify(jdbcTemplate, never()).update(eq(NuraQueryConstants.UPDATE_AUDIT_QUERY_WITH_TIMESTAMP), any(), any(), any());
    }

    @Test
    public void testProcessFailedRequests_retriesExhausted() {
        Map<String, Object> record = createTestRecord("3", "{\"key\":\"value\"}", "GTID", "key:value");
        when(jdbcTemplate.queryForList(NuraQueryConstants.FAILED_AUDIT_QUERY)).thenReturn(List.of(record));
        when(jdbcTemplate.queryForObject(anyString(), any(Object[].class), eq(String.class)))
                .thenReturn("{\"ENVIRONMENT\":\"PRODN\"}");
        when(propertyConstants.getNyurl()).thenReturn("http://bulk-api");

        when(restTemplateService.sendRequest(anyString(), any(), anyString(), anyMap()))
                .thenReturn(new ResponseEntity<>("Fail", HttpStatus.INTERNAL_SERVER_ERROR));

        ResponseEntity<String> healthCheck = new ResponseEntity<>("UP", HttpStatus.OK);
        when(scheduler.restTemplate.getForEntity(anyString(), eq(String.class))).thenReturn(healthCheck);

        scheduler.processFailedRequests();

        verify(jdbcTemplate).update(eq(NuraQueryConstants.UPDATE_AUDIT_QUERY_WITH_TIMESTAMP), eq("Failed"), eq("Fail"), eq(3L));
    }

    @Test
    public void testProcessFailedRequests_sparkHealthDown() {
        ResponseEntity<String> healthCheck = new ResponseEntity<>("DOWN", HttpStatus.INTERNAL_SERVER_ERROR);
        when(scheduler.restTemplate.getForEntity(anyString(), eq(String.class))).thenReturn(healthCheck);

        scheduler.processFailedRequests();

        verify(jdbcTemplate, never()).queryForList(NuraQueryConstants.FAILED_AUDIT_QUERY);
    }

    private Map<String, Object> createTestRecord(String id, String payload, String gtid, String headers) {
        Map<String, Object> record = new HashMap<>();
        record.put(AuditTableConstants.ID, id);
        record.put(AuditTableConstants.REQUEST_PAYLOAD, payload);
        record.put(AuditTableConstants.GLOBAL_TRANSACTION_ID, gtid);
        record.put(AuditTableConstants.HEADERS, headers);
        return record;
    }
}