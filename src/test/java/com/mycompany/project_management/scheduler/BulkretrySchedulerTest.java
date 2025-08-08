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
class BulkRetryScheduler1Test {

    @MockBean
    private JdbcTemplate jdbcTemplate;

    @MockBean
    private RestTemplateService restTemplateService;

    @MockBean
    private UrlUtils urlUtils;

    @Autowired
    private BulkRetryScheduler1 scheduler;

    private Map<String, Object> sampleRecord;

    @BeforeEach
    void setup() {
        sampleRecord = Map.of(
                "ID", 1L,
                "API_ENDPOINT", "http://localhost/api",
                "REQUEST_PAYLOAD", "{}",
                "HEADERS", "{}"
        );

        when(urlUtils.extractBaseUrl(anyString()))
                .thenReturn("http://localhost");

        when(jdbcTemplate.queryForList(anyString()))
                .thenReturn(List.of(sampleRecord));
    }

    @Test
    void testProcessFailedRequests_HealthDown() {
        when(restTemplateService.getForEntity(
                eq("http://localhost" + AuditTableConstants.HEALTH_CHECK_ENDPOINT),
                eq(String.class)))
                .thenReturn(new ResponseEntity<>("DOWN", HttpStatus.SERVICE_UNAVAILABLE));

        scheduler.processFailedRequests();

        verify(restTemplateService).getForEntity(anyString(), eq(String.class));
    }

    @Test
    void testProcessFailedRequests_Success200() {
        when(restTemplateService.getForEntity(anyString(), eq(String.class)))
                .thenReturn(new ResponseEntity<>("UP", HttpStatus.OK));

        when(restTemplateService.sendRequest(anyString(), eq(HttpMethod.POST), any(), anyMap()))
                .thenReturn(new ResponseEntity<>("OK", HttpStatus.OK));

        scheduler.processFailedRequests();

        verify(restTemplateService).sendRequest(anyString(), eq(HttpMethod.POST), any(), anyMap());
    }

    @Test
    void testProcessFailedRequests_ClientError4xx() {
        when(restTemplateService.getForEntity(anyString(), eq(String.class)))
                .thenReturn(new ResponseEntity<>("UP", HttpStatus.OK));

        when(restTemplateService.sendRequest(anyString(), eq(HttpMethod.POST), any(), anyMap()))
                .thenReturn(new ResponseEntity<>("Bad Request", HttpStatus.BAD_REQUEST));

        scheduler.processFailedRequests();

        verify(restTemplateService).sendRequest(anyString(), eq(HttpMethod.POST), any(), anyMap());
    }

    @Test
    void testProcessFailedRequests_ServerError5xx() {
        when(restTemplateService.getForEntity(anyString(), eq(String.class)))
                .thenReturn(new ResponseEntity<>("UP", HttpStatus.OK));

        when(restTemplateService.sendRequest(anyString(), eq(HttpMethod.POST), any(), anyMap()))
                .thenReturn(new ResponseEntity<>("Server Error", HttpStatus.INTERNAL_SERVER_ERROR));

        scheduler.processFailedRequests();

        verify(restTemplateService).sendRequest(anyString(), eq(HttpMethod.POST), any(), anyMap());
    }

    @Test
    void testProcessFailedRequests_UnexpectedStatus() {
        when(restTemplateService.getForEntity(anyString(), eq(String.class)))
                .thenReturn(new ResponseEntity<>("UP", HttpStatus.OK));

        when(restTemplateService.sendRequest(anyString(), eq(HttpMethod.POST), any(), anyMap()))
                .thenReturn(new ResponseEntity<>("Weird", HttpStatus.I_AM_A_TEAPOT));

        scheduler.processFailedRequests();

        verify(restTemplateService).sendRequest(anyString(), eq(HttpMethod.POST), any(), anyMap());
    }

    @Test
    void testProcessFailedRequests_Exception() {
        when(restTemplateService.getForEntity(anyString(), eq(String.class)))
                .thenThrow(new RuntimeException("Test Exception"));

        scheduler.processFailedRequests();

        verify(restTemplateService).getForEntity(anyString(), eq(String.class));
    }
}

