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
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class BulkRetrySchedulerTest {

    @InjectMocks
    private BulkRetryScheduler bulkRetryScheduler;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private RestTemplateService restTemplateService;

    @Mock
    private PropertyConstants propertyConstants;

    @Mock
    private RetryTemplate retryTemplate;

    @Mock
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(bulkRetryScheduler, "healthCheckUrl", "http://localhost/health");
        ReflectionTestUtils.setField(bulkRetryScheduler, "rateLimit", 10.0);
        ReflectionTestUtils.setField(bulkRetryScheduler, "burstTime", 1L);
        ReflectionTestUtils.setField(bulkRetryScheduler, "restTemplate", restTemplate);
        bulkRetryScheduler.initRateLimiter();
    }

    @Test
    void testProcessFailedRequests_SuccessfulFlow() {
        // Health check
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenReturn(new ResponseEntity<>("OK", HttpStatus.OK));

        // Failed records
        Map<String, Object> row = new HashMap<>();
        row.put("ID", 1L);
        row.put("REQUEST_PAYLOAD", "{\"data\":\"test\"}");
        row.put("GLOBAL_TRANSACTION_ID", "txn123");
        row.put("HEADERS", "{key1=value1,key2=value2}");
        when(jdbcTemplate.queryForList(anyString())).thenReturn(List.of(row));

        // Headers query
        when(jdbcTemplate.queryForObject(anyString(), any(Object[].class), eq(String.class)))
                .thenReturn("{\"environment\":\"PRODNY\"}");

        // URL resolution
        when(propertyConstants.getUrl()).thenReturn("http://default.url");
        when(propertyConstants.getNyurl()).thenReturn("http://ny.url");

        // Retry template
        when(retryTemplate.execute(any())).thenAnswer(invocation -> {
            RetryCallback<ResponseEntity<String>, ?> callback = invocation.getArgument(0);
            return callback.doWithRetry(mock(RetryContext.class));
        });

        // REST call
        when(restTemplateService.sendRequest(anyString(), eq(HttpMethod.POST), any(), anyMap()))
                .thenReturn(new ResponseEntity<>("Success", HttpStatus.OK));

        bulkRetryScheduler.processFailedRequests();

        verify(jdbcTemplate).update(anyString(), eq("SUCCESS"), eq("Success"), eq(1L));
    }

    @Test
    void testProcessFailedRequests_ClientError() {
        simulateOneRecord();

        when(restTemplateService.sendRequest(anyString(), eq(HttpMethod.POST), any(), anyMap()))
                .thenReturn(new ResponseEntity<>("Bad Request", HttpStatus.BAD_REQUEST));

        bulkRetryScheduler.processFailedRequests();

        verify(jdbcTemplate, never()).update(anyString(), any(), any(), any());
    }
    
    @Test
    void testProcessFailedRequests_ServerError() {
        simulateOneRecord();

        when(restTemplateService.sendRequest(anyString(), eq(HttpMethod.POST), any(), anyMap()))
                .thenReturn(new ResponseEntity<>("Server Error", HttpStatus.INTERNAL_SERVER_ERROR));

        bulkRetryScheduler.processFailedRequests();

        verify(jdbcTemplate, never()).update(anyString(), any(), any(), any());
    }
    @Test
    void testProcessFailedRequests_UnexpectedStatus() {
        simulateOneRecord();

        when(restTemplateService.sendRequest(anyString(), eq(HttpMethod.POST), any(), anyMap()))
                .thenReturn(new ResponseEntity<>("Something weird", HttpStatus.I_AM_A_TEAPOT));

        bulkRetryScheduler.processFailedRequests();

        verify(jdbcTemplate).update(anyString(), eq("FAILED"), eq("Something weird"), eq(1L));
    }

    @Test
    void testProcessFailedRequests_ExceptionHandling() {
        simulateOneRecord();

        when(restTemplateService.sendRequest(anyString(), eq(HttpMethod.POST), any(), anyMap()))
                .thenThrow(new RuntimeException("API error"));

        bulkRetryScheduler.processFailedRequests();

        // Should not throw, should be caught
    }

     @Test
    void testProcessFailedRequests_HealthCheckDown() {
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenReturn(new ResponseEntity<>("Down", HttpStatus.SERVICE_UNAVAILABLE));

        bulkRetryScheduler.processFailedRequests();

        verify(jdbcTemplate, never()).queryForList(anyString());
    }

    private void simulateOneRecord() {
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenReturn(new ResponseEntity<>("OK", HttpStatus.OK));

        Map<String, Object> row = new HashMap<>();
        row.put("ID", 1L);
        row.put("REQUEST_PAYLOAD", "{\"data\":\"test\"}");
        row.put("GLOBAL_TRANSACTION_ID", "txn123");
        row.put("HEADERS", "{key1=value1}");

        when(jdbcTemplate.queryForList(anyString())).thenReturn(List.of(row));
        when(jdbcTemplate.queryForObject(anyString(), any(Object[].class), eq(String.class)))
                .thenReturn("{\"environment\":\"PRODNY\"}");

        when(propertyConstants.getNyurl()).thenReturn("http://ny.url");

        when(retryTemplate.execute(any())).thenAnswer(invocation -> {
            RetryCallback<ResponseEntity<String>, ?> callback = invocation.getArgument(0);
            return callback.doWithRetry(mock(RetryContext.class));
        });
    }
}
