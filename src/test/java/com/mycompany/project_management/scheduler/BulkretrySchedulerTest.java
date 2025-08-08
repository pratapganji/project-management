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
    private RetryTemplate retryTemplate;

    @Mock
    private RateLimiter rateLimiterMock;

    @Spy
    private org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        // Set values for @Value fields
        Field rateLimitField = BulkRetryScheduler.class.getDeclaredField("rateLimit");
        rateLimitField.setAccessible(true);
        rateLimitField.set(scheduler, 10.0);

        Field burstTimeField = BulkRetryScheduler.class.getDeclaredField("burstTime");
        burstTimeField.setAccessible(true);
        burstTimeField.set(scheduler, 1L);

        // Inject mock RateLimiter instead of real one
        Field rateLimiterField = BulkRetryScheduler.class.getDeclaredField("rateLimiter");
        rateLimiterField.setAccessible(true);
        rateLimiterField.set(scheduler, rateLimiterMock);

        // Avoid blocking
        when(rateLimiterMock.acquire()).thenReturn(1.0);
    }

    @Test
    void testInitRateLimiter() throws Exception {
        BulkRetryScheduler newScheduler = new BulkRetryScheduler();
        Field rateLimitField = BulkRetryScheduler.class.getDeclaredField("rateLimit");
        rateLimitField.setAccessible(true);
        rateLimitField.set(newScheduler, 5.0);

        Field burstTimeField = BulkRetryScheduler.class.getDeclaredField("burstTime");
        burstTimeField.setAccessible(true);
        burstTimeField.set(newScheduler, 2L);

        newScheduler.initRateLimiter();

        Field rateLimiterField = BulkRetryScheduler.class.getDeclaredField("rateLimiter");
        rateLimiterField.setAccessible(true);
        RateLimiter rl = (RateLimiter) rateLimiterField.get(newScheduler);
        assert rl != null;
    }

    @Test
    void testProcessFailedRequests_NoRecords() {
        when(jdbcTemplate.queryForList(anyString())).thenReturn(Collections.emptyList());
        scheduler.processFailedRequests();
        verify(jdbcTemplate).queryForList(anyString());
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

        try (MockedStatic<UrlUtils> mockedStatic = mockStatic(UrlUtils.class)) {
            mockedStatic.when(() -> UrlUtils.extractBaseUrl("http://localhost/api"))
                    .thenReturn("http://localhost");

            when(restTemplate.getForEntity(eq("http://localhost" + AuditTableConstants.HEALTH_CHECK_ENDPOINT), eq(String.class)))
                    .thenReturn(new ResponseEntity<>("DOWN", HttpStatus.SERVICE_UNAVAILABLE));

            scheduler.processFailedRequests();

            verify(restTemplate).getForEntity(anyString(), eq(String.class));
        }
    }

    @Test
    void testProcessFailedRequests_Success200() throws Exception {
        Map<String, Object> record = Map.of(
                "ID", 1L,
                "API_ENDPOINT", "http://localhost/api",
                "REQUEST_PAYLOAD", "{}",
                "HEADERS", "{key:value}"
        );

        when(jdbcTemplate.queryForList(anyString())).thenReturn(List.of(record));
        when(restTemplate.getForEntity(contains("/health"), eq(String.class)))
                .thenReturn(new ResponseEntity<>("UP", HttpStatus.OK));

        when(retryTemplate.execute(any())).thenAnswer(inv -> {
            RetryCallback<ResponseEntity<String>, Exception> cb = inv.getArgument(0);
            return cb.doWithRetry(mock(RetryContext.class));
        });

        when(restTemplateService.sendRequest(anyString(), eq(HttpMethod.POST), anyString(), anyMap()))
                .thenReturn(new ResponseEntity<>("OK", HttpStatus.OK));

        scheduler.processFailedRequests();

        verify(jdbcTemplate).update(anyString(), eq("SUCCESS"), eq("OK"), eq(1L));
    }

    @Test
    void testProcessFailedRequests_ClientError4xx() {
        Map<String, Object> record = Map.of(
                "ID", 1L,
                "API_ENDPOINT", "http://localhost/api",
                "REQUEST_PAYLOAD", "{}",
                "HEADERS", "{key:value}"
        );

        when(jdbcTemplate.queryForList(anyString())).thenReturn(List.of(record));
        when(restTemplate.getForEntity(contains("/health"), eq(String.class)))
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

    @Test
    void testProcessFailedRequests_ServerError5xx() {
        Map<String, Object> record = Map.of(
                "ID", 1L,
                "API_ENDPOINT", "http://localhost/api",
                "REQUEST_PAYLOAD", "{}",
                "HEADERS", "{key:value}"
        );

        when(jdbcTemplate.queryForList(anyString())).thenReturn(List.of(record));
        when(restTemplate.getForEntity(contains("/health"), eq(String.class)))
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

    @Test
    void testProcessFailedRequests_UnexpectedStatus() {
        Map<String, Object> record = Map.of(
                "ID", 1L,
                "API_ENDPOINT", "http://localhost/api",
                "REQUEST_PAYLOAD", "{}",
                "HEADERS", "{key:value}"
        );

        when(jdbcTemplate.queryForList(anyString())).thenReturn(List.of(record));
        when(restTemplate.getForEntity(contains("/health"), eq(String.class)))
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

    @Test
    void testProcessFailedRequests_Exception() {
        Map<String, Object> record = Map.of(
                "ID", 1L,
                "API_ENDPOINT", "http://localhost/api",
                "REQUEST_PAYLOAD", "{}",
                "HEADERS", "{key:value}"
        );

        when(jdbcTemplate.queryForList(anyString())).thenReturn(List.of(record));
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenThrow(new RuntimeException("Boom"));

        scheduler.processFailedRequests();
    }

    @Test
    void testProcessHeaders() throws Exception {
        var method = BulkRetryScheduler.class.getDeclaredMethod("processHeaders", String.class);
        method.setAccessible(true);
        Map<String, String> map = (Map<String, String>) method.invoke(scheduler, "{a:b, c:d}");
        assert map.size() == 2;
        assert map.get("a").equals("b");
    }
}
