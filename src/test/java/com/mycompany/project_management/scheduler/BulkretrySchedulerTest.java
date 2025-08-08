import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.google.common.util.concurrent.RateLimiter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.support.RetryTemplate;

import java.lang.reflect.Field;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.google.common.util.concurrent.RateLimiter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.support.RetryTemplate;

import java.lang.reflect.Field;
import java.util.*;

@ExtendWith(MockitoExtension.class)
class BulkRetrySchedulerTest {

    @InjectMocks
    private BulkRetryScheduler scheduler;

    @Mock
    private JdbcTemplate jdbcTemplate;
    @Mock
    private RestTemplateService restTemplateService;
    @Mock
    private PropertyConstants propertyConstants;
    @Mock
    private RetryTemplate retryTemplate;
    @Mock
    private UrlUtils urlUtils;

    @Captor
    ArgumentCaptor<String> sqlCaptor;

    @BeforeEach
    void setUp() {
        // Inject test values for @Value fields
        scheduler.rateLimit = 10.0;
        scheduler.burstTime = 1;
    }

    @Test
    void testInitRateLimiter() {
        scheduler.initRateLimiter();
        // No exception means success, coverage for initRateLimiter
    }

    @Test
    void testProcessFailedRequests_NoRecords() {
        when(jdbcTemplate.queryForList(anyString())).thenReturn(Collections.emptyList());
        scheduler.processFailedRequests();
        verify(jdbcTemplate, times(1)).queryForList(anyString());
    }

    @Test
    void testProcessFailedRequests_Success200() throws Exception {
        Map<String, Object> record = createRecord();
        when(jdbcTemplate.queryForList(anyString())).thenReturn(List.of(record));
        when(urlUtils.extractBaseUrl(anyString())).thenReturn("http://baseurl");
        mockHealthCheck(HttpStatus.OK);

        mockRetryTemplateExecution(HttpStatus.OK, "SuccessBody");

        scheduler.initRateLimiter();
        scheduler.processFailedRequests();

        verify(jdbcTemplate).update(anyString(), eq("SUCCESS"), eq("SuccessBody"), eq(1L));
    }

    @Test
    void testProcessFailedRequests_ClientError4xx() throws Exception {
        Map<String, Object> record = createRecord();
        when(jdbcTemplate.queryForList(anyString())).thenReturn(List.of(record));
        when(urlUtils.extractBaseUrl(anyString())).thenReturn("http://baseurl");
        mockHealthCheck(HttpStatus.OK);

        mockRetryTemplateExecution(HttpStatus.BAD_REQUEST, "ClientError");

        scheduler.initRateLimiter();
        scheduler.processFailedRequests();

        verify(jdbcTemplate).update(anyString(), eq("FAILED"), eq("ClientError"), eq(1L));
    }

    @Test
    void testProcessFailedRequests_ServerError5xx() throws Exception {
        Map<String, Object> record = createRecord();
        when(jdbcTemplate.queryForList(anyString())).thenReturn(List.of(record));
        when(urlUtils.extractBaseUrl(anyString())).thenReturn("http://baseurl");
        mockHealthCheck(HttpStatus.OK);

        mockRetryTemplateExecution(HttpStatus.INTERNAL_SERVER_ERROR, "ServerError");

        scheduler.initRateLimiter();
        scheduler.processFailedRequests();

        // No DB update for 5xx
        verify(jdbcTemplate, never()).update(anyString(), any(), any(), any());
    }

    @Test
    void testProcessFailedRequests_UnexpectedStatus() throws Exception {
        Map<String, Object> record = createRecord();
        when(jdbcTemplate.queryForList(anyString())).thenReturn(List.of(record));
        when(urlUtils.extractBaseUrl(anyString())).thenReturn("http://baseurl");
        mockHealthCheck(HttpStatus.OK);

        mockRetryTemplateExecution(HttpStatus.MULTIPLE_CHOICES, "Unexpected");

        scheduler.initRateLimiter();
        scheduler.processFailedRequests();

        verify(jdbcTemplate).update(anyString(), eq("FAILED"), eq("Unexpected"), eq(1L));
    }

    @Test
    void testProcessFailedRequests_HealthDown() {
        Map<String, Object> record = createRecord();
        when(jdbcTemplate.queryForList(anyString())).thenReturn(List.of(record));
        when(urlUtils.extractBaseUrl(anyString())).thenReturn("http://baseurl");
        mockHealthCheck(HttpStatus.SERVICE_UNAVAILABLE);

        scheduler.initRateLimiter();
        scheduler.processFailedRequests();

        verify(jdbcTemplate, never()).update(anyString(), any(), any(), any());
    }

    @Test
    void testProcessFailedRequests_Exception() {
        when(jdbcTemplate.queryForList(anyString())).thenThrow(new RuntimeException("DB error"));

        scheduler.processFailedRequests();
    }

    @Test
    void testProcessHeaders() {
        String headers = "{key1:value1, key2:value2}";
        Map<String, String> result = invokeProcessHeaders(headers);
        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals("value1", result.get("key1"));
    }

    @Test
    void testUpdateStatusWithTimestamp() {
        invokeUpdateStatusWithTimestamp(1L, "SUCCESS", "body");
        verify(jdbcTemplate).update(anyString(), eq("SUCCESS"), eq("body"), eq(1L));
    }

    // ---------------- Helpers ----------------

    private Map<String, Object> createRecord() {
        Map<String, Object> record = new HashMap<>();
        record.put("ID", 1L);
        record.put("API_ENDPOINT", "http://api/test");
        record.put("REQUEST_PAYLOAD", "payload");
        record.put("HEADERS", "{Content-Type:application/json}");
        return record;
    }

    private void mockHealthCheck(HttpStatus status) {
        when(scheduler.restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenReturn(new ResponseEntity<>("health", status));
    }

    private void mockRetryTemplateExecution(HttpStatus status, String body) throws Exception {
        when(retryTemplate.execute(any())).thenAnswer(invocation -> {
            RetryCallback<ResponseEntity<String>, Exception> callback = invocation.getArgument(0);
            return callback.doWithRetry(mockRetryContext(status, body));
        });
    }

    private RetryContext mockRetryContext(HttpStatus status, String body) {
        RetryContext context = mock(RetryContext.class);
        when(context.getRetryCount()).thenReturn(0);
        ResponseEntity<String> response = new ResponseEntity<>(body, status);
        when(restTemplateService.sendRequest(anyString(), eq(HttpMethod.POST), any(), anyMap()))
                .thenReturn(response);
        return context;
    }

    // Reflection to call private methods
    private Map<String, String> invokeProcessHeaders(String headers) {
        try {
            var method = BulkRetryScheduler.class.getDeclaredMethod("processHeaders", String.class);
            method.setAccessible(true);
            return (Map<String, String>) method.invoke(scheduler, headers);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void invokeUpdateStatusWithTimestamp(Long id, String status, String body) {
        try {
            var method = BulkRetryScheduler.class.getDeclaredMethod("updateStatusWithTimestamp", Long.class, String.class, String.class);
            method.setAccessible(true);
            method.invoke(scheduler, id, status, body);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
