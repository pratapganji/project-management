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
    private RateLimiter rateLimiterMock;

    @Spy
    private org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        // Set @Value fields
        setPrivateField("rateLimit", 10.0);
        setPrivateField("burstTime", 1L);

        // Inject mock RateLimiter
        setPrivateField("rateLimiter", rateLimiterMock);
        when(rateLimiterMock.acquire()).thenReturn(1.0);
    }

    private void setPrivateField(String fieldName, Object value) throws Exception {
        Field field = BulkRetryScheduler.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(scheduler, value);
    }

    @Test
    void testInitRateLimiter() throws Exception {
        BulkRetryScheduler newScheduler = new BulkRetryScheduler();
        setField(newScheduler, "rateLimit", 5.0);
        setField(newScheduler, "burstTime", 2L);

        newScheduler.initRateLimiter();

        Field f = BulkRetryScheduler.class.getDeclaredField("rateLimiter");
        f.setAccessible(true);
        assert f.get(newScheduler) != null;
    }

    @Test
    void testProcessFailedRequests_NoRecords() {
        when(jdbcTemplate.queryForList(anyString())).thenReturn(Collections.emptyList());
        scheduler.processFailedRequests();
        verify(jdbcTemplate).queryForList(anyString());
    }

    @Test
    void testProcessFailedRequests_HealthDown() {
        Map<String, Object> record = baseRecord();
        when(jdbcTemplate.queryForList(anyString())).thenReturn(List.of(record));

        try (MockedStatic<UrlUtils> mockedStatic = mockStatic(UrlUtils.class)) {
            mockedStatic.when(() -> UrlUtils.extractBaseUrl("http://localhost/api"))
                    .thenReturn("http://localhost");

            when(restTemplate.getForEntity(eq("http://localhost" + AuditTableConstants.HEALTH_CHECK_ENDPOINT),
                    eq(String.class)))
                    .thenReturn(new ResponseEntity<>("DOWN", HttpStatus.SERVICE_UNAVAILABLE));

            scheduler.processFailedRequests();
            verify(restTemplate).getForEntity(anyString(), eq(String.class));
        }
    }

    @Test
    void testProcessFailedRequests_Success200() {
        Map<String, Object> record = baseRecord();
        when(jdbcTemplate.queryForList(anyString())).thenReturn(List.of(record));

        try (MockedStatic<UrlUtils> mockedStatic = mockStatic(UrlUtils.class)) {
            mockedStatic.when(() -> UrlUtils.extractBaseUrl("http://localhost/api"))
                    .thenReturn("http://localhost");

            when(restTemplate.getForEntity(anyString(), eq(String.class)))
                    .thenReturn(new ResponseEntity<>("UP", HttpStatus.OK));

            when(retryTemplate.execute(any())).thenAnswer(inv -> {
                RetryCallback<ResponseEntity<String>, Exception> cb = inv.getArgument(0);
                return cb.doWithRetry(mock(RetryContext.class));
            });

            when(restTemplateService.sendRequest(anyString(), eq(HttpMethod.POST), anyString(), anyMap()))
                    .thenReturn(new ResponseEntity<>("OK", HttpStatus.OK));

            scheduler.processFailedRequests();

            verify(jdbcTemplate).update(anyString(), eq("SUCCESS"), eq("OK"), eq(1L));
            verify(rateLimiterMock).acquire();
        }
    }

    @Test
    void testProcessFailedRequests_ClientError4xx() {
        Map<String, Object> record = baseRecord();
        when(jdbcTemplate.queryForList(anyString())).thenReturn(List.of(record));

        try (MockedStatic<UrlUtils> mockedStatic = mockStatic(UrlUtils.class)) {
            mockedStatic.when(() -> UrlUtils.extractBaseUrl("http://localhost/api"))
                    .thenReturn("http://localhost");

            when(restTemplate.getForEntity(anyString(), eq(String.class)))
                    .thenReturn(new ResponseEntity<>("UP", HttpStatus.OK));

            when(retryTemplate.execute(any())).thenAnswer(inv -> {
                RetryCallback<ResponseEntity<String>, Exception> cb = inv.getArgument(0);
                return cb.doWithRetry(mock(RetryContext.class));
            });

            when(restTemplateService.sendRequest(anyString(), eq(HttpMethod.POST), anyString(), anyMap()))
                    .thenReturn(new ResponseEntity<>("Bad Request", HttpStatus.BAD_REQUEST));

            scheduler.processFailedRequests();

            verify(jdbcTemplate).update(anyString(), eq("FAILED"), eq("Bad Request"), eq(1L));
        }
    }

    @Test
    void testProcessFailedRequests_ServerError5xx() {
        Map<String, Object> record = baseRecord();
        when(jdbcTemplate.queryForList(anyString())).thenReturn(List.of(record));

        try (MockedStatic<UrlUtils> mockedStatic = mockStatic(UrlUtils.class)) {
            mockedStatic.when(() -> UrlUtils.extractBaseUrl("http://localhost/api"))
                    .thenReturn("http://localhost");

            when(restTemplate.getForEntity(anyString(), eq(String.class)))
                    .thenReturn(new ResponseEntity<>("UP", HttpStatus.OK));

            when(retryTemplate.execute(any())).thenAnswer(inv -> {
                RetryCallback<ResponseEntity<String>, Exception> cb = inv.getArgument(0);
                return cb.doWithRetry(mock(RetryContext.class));
            });

            when(restTemplateService.sendRequest(anyString(), eq(HttpMethod.POST), anyString(), anyMap()))
                    .thenReturn(new ResponseEntity<>("Error", HttpStatus.INTERNAL_SERVER_ERROR));

            scheduler.processFailedRequests();

            verify(jdbcTemplate, never()).update(anyString(), any(), any(), any());
        }
    }

    @Test
    void testProcessFailedRequests_UnexpectedStatus() {
        Map<String, Object> record = baseRecord();
        when(jdbcTemplate.queryForList(anyString())).thenReturn(List.of(record));

        try (MockedStatic<UrlUtils> mockedStatic = mockStatic(UrlUtils.class)) {
            mockedStatic.when(() -> UrlUtils.extractBaseUrl("http://localhost/api"))
                    .thenReturn("http://localhost");

            when(restTemplate.getForEntity(anyString(), eq(String.class)))
                    .thenReturn(new ResponseEntity<>("UP", HttpStatus.OK));

            when(retryTemplate.execute(any())).thenAnswer(inv -> {
                RetryCallback<ResponseEntity<String>, Exception> cb = inv.getArgument(0);
                return cb.doWithRetry(mock(RetryContext.class));
            });

            when(restTemplateService.sendRequest(anyString(), eq(HttpMethod.POST), anyString(), anyMap()))
                    .thenReturn(new ResponseEntity<>("Weird", HttpStatus.I_AM_A_TEAPOT));

            scheduler.processFailedRequests();

            verify(jdbcTemplate).update(anyString(), eq("FAILED"), eq("Weird"), eq(1L));
        }
    }

    @Test
    void testProcessFailedRequests_Exception() {
        Map<String, Object> record = baseRecord();
        when(jdbcTemplate.queryForList(anyString())).thenReturn(List.of(record));

        try (MockedStatic<UrlUtils> mockedStatic = mockStatic(UrlUtils.class)) {
            mockedStatic.when(() -> UrlUtils.extractBaseUrl("http://localhost/api"))
                    .thenReturn("http://localhost");

            when(restTemplate.getForEntity(anyString(), eq(String.class)))
                    .thenThrow(new RuntimeException("Boom"));

            scheduler.processFailedRequests();
        }
    }

    @Test
    void testProcessHeaders() throws Exception {
        var method = BulkRetryScheduler.class.getDeclaredMethod("processHeaders", String.class);
        method.setAccessible(true);
        Map<String, String> map = (Map<String, String>) method.invoke(scheduler, "{a:b, c:d}");
        assert map.size() == 2;
        assert map.get("a").equals("b");
    }

    // Helper to avoid repetition
    private Map<String, Object> baseRecord() {
        return Map.of(
                "ID", 1L,
                "API_ENDPOINT", "http://localhost/api",
                "REQUEST_PAYLOAD", "{}",
                "HEADERS", "{key:value}"
        );
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field f = BulkRetryScheduler.class.getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }
}

