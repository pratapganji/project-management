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

    @InjectMocks
    private BulkRetryScheduler1 scheduler;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private RestTemplateService restTemplateService;

    @Mock
    private UrlUtils urlUtils;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(urlUtils.extractBaseUrl(anyString())).thenReturn("http://localhost");
    }

    @Test
    void testProcessFailedRequests_HealthDown() {
        Map<String, Object> record = Map.of(
                "ID", 1L,
                "API_ENDPOINT", "http://localhost/api",
                "REQUEST_PAYLOAD", "{}",
                "HEADERS", "{}"
        );
        when(jdbcTemplate.queryForList(anyString())).thenReturn(List.of(record));
        when(restTemplateService.sendRequest(anyString(), eq(HttpMethod.POST), any(), anyMap()))
                .thenReturn(new ResponseEntity<>("DOWN", HttpStatus.SERVICE_UNAVAILABLE));

        scheduler.processFailedRequests();

        verify(urlUtils).extractBaseUrl("http://localhost/api");
        verify(restTemplateService).sendRequest(anyString(), eq(HttpMethod.POST), any(), anyMap());
    }

    @Test
    void testProcessFailedRequests_Success200() {
        Map<String, Object> record = Map.of(
                "ID", 2L,
                "API_ENDPOINT", "http://localhost/api",
                "REQUEST_PAYLOAD", "{}",
                "HEADERS", "{}"
        );
        when(jdbcTemplate.queryForList(anyString())).thenReturn(List.of(record));
        when(restTemplateService.sendRequest(anyString(), eq(HttpMethod.POST), any(), anyMap()))
                .thenReturn(new ResponseEntity<>("OK", HttpStatus.OK));

        scheduler.processFailedRequests();

        verify(restTemplateService).sendRequest(anyString(), eq(HttpMethod.POST), any(), anyMap());
    }

    @Test
    void testProcessFailedRequests_ClientError4xx() {
        Map<String, Object> record = Map.of(
                "ID", 3L,
                "API_ENDPOINT", "http://localhost/api",
                "REQUEST_PAYLOAD", "{}",
                "HEADERS", "{}"
        );
        when(jdbcTemplate.queryForList(anyString())).thenReturn(List.of(record));
        when(restTemplateService.sendRequest(anyString(), eq(HttpMethod.POST), any(), anyMap()))
                .thenReturn(new ResponseEntity<>("Bad Request", HttpStatus.BAD_REQUEST));

        scheduler.processFailedRequests();

        verify(restTemplateService).sendRequest(anyString(), eq(HttpMethod.POST), any(), anyMap());
    }

    @Test
    void testProcessFailedRequests_ServerError5xx() {
        Map<String, Object> record = Map.of(
                "ID", 4L,
                "API_ENDPOINT", "http://localhost/api",
                "REQUEST_PAYLOAD", "{}",
                "HEADERS", "{}"
        );
        when(jdbcTemplate.queryForList(anyString())).thenReturn(List.of(record));
        when(restTemplateService.sendRequest(anyString(), eq(HttpMethod.POST), any(), anyMap()))
                .thenReturn(new ResponseEntity<>("Internal Error", HttpStatus.INTERNAL_SERVER_ERROR));

        scheduler.processFailedRequests();

        verify(restTemplateService).sendRequest(anyString(), eq(HttpMethod.POST), any(), anyMap());
    }

    @Test
    void testProcessFailedRequests_UnexpectedStatus() {
        Map<String, Object> record = Map.of(
                "ID", 5L,
                "API_ENDPOINT", "http://localhost/api",
                "REQUEST_PAYLOAD", "{}",
                "HEADERS", "{}"
        );
        when(jdbcTemplate.queryForList(anyString())).thenReturn(List.of(record));
        when(restTemplateService.sendRequest(anyString(), eq(HttpMethod.POST), any(), anyMap()))
                .thenReturn(new ResponseEntity<>("Strange", HttpStatus.I_AM_A_TEAPOT)); // 418

        scheduler.processFailedRequests();

        verify(restTemplateService).sendRequest(anyString(), eq(HttpMethod.POST), any(), anyMap());
    }

    @Test
    void testProcessFailedRequests_Exception() {
        Map<String, Object> record = Map.of(
                "ID", 6L,
                "API_ENDPOINT", "http://localhost/api",
                "REQUEST_PAYLOAD", "{}",
                "HEADERS", "{}"
        );
        when(jdbcTemplate.queryForList(anyString())).thenReturn(List.of(record));
        when(restTemplateService.sendRequest(anyString(), eq(HttpMethod.POST), any(), anyMap()))
                .thenThrow(new RuntimeException("Something went wrong"));

        scheduler.processFailedRequests();

        verify(restTemplateService).sendRequest(anyString(), eq(HttpMethod.POST), any(), anyMap());
    }
}

