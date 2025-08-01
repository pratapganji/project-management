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
public class BulkretrySchedulerTest {

    @Mock private JdbcTemplate jdbcTemplate;
    @Mock private RestTemplateService restTemplateService;
    @Mock private PropertyConstants propertyConstants;
    @InjectMocks private BulkRetryScheduler scheduler;

    @Mock private RestTemplate restTemplate;

    private final String HEALTH_URL = "http://mock-health";
    private final String RECORD_ID = "123";

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(scheduler, "rateLimit", 10.0);
        ReflectionTestUtils.setField(scheduler, "burstTime", 1L);
        ReflectionTestUtils.setField(scheduler, "maxRetries", 2);
        ReflectionTestUtils.setField(scheduler, "healthCheckUrl", HEALTH_URL);
        ReflectionTestUtils.setField(scheduler, "backoffMillis", 10L); // quick retry
        ReflectionTestUtils.setField(scheduler, "restTemplate", restTemplate);
        scheduler.initRateLimiter();
    }

    private Map<String, Object> buildRecord(String env, String headers) {
        Map<String, Object> record = new HashMap<>();
        record.put("ID", RECORD_ID);
        record.put("REQUEST_PAYLOAD", "{\"data\":\"test\"}");
        record.put("GLOBAL_TRANSACTION_ID", "txn-1");
        record.put("HEADERS", headers);
        when(jdbcTemplate.queryForObject(anyString(), any(Object[].class), eq(String.class)))
                .thenReturn("{ENVIRONMENT:" + env + "}");
        return record;
    }

    @Test
    public void testHealthCheckDown_skipsProcessing() {
        when(restTemplate.getForEntity(HEALTH_URL, String.class))
                .thenReturn(new ResponseEntity<>("DOWN", HttpStatus.INTERNAL_SERVER_ERROR));
        scheduler.processFailedRequests();
        verify(jdbcTemplate, never()).queryForList(anyString());
    }

    @Test
    public void testEnvironmentIsPRODNY_usesNyUrl() {
        Map<String, Object> record = buildRecord("PRODNY", "{Authorization:Bearer}");
        when(jdbcTemplate.queryForList(anyString())).thenReturn(List.of(record));
        when(propertyConstants.getNyurl()).thenReturn("ny-url");
        when(propertyConstants.getUrl()).thenReturn("default-url");

        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenReturn(new ResponseEntity<>("OK", HttpStatus.OK));

        when(restTemplateService.sendRequest(any(), any(), any(), any()))
                .thenReturn(new ResponseEntity<>("ok", HttpStatus.OK));

        scheduler.processFailedRequests();

        verify(jdbcTemplate).update(anyString(), eq("Success"), eq("ok"), eq(Long.parseLong(RECORD_ID)));
    }

    @Test
    public void testEnvironmentIsPRODNJ_usesNjUrl() {
        Map<String, Object> record = buildRecord("PRODNJ", "{Authorization:Bearer}");
        when(jdbcTemplate.queryForList(anyString())).thenReturn(List.of(record));
        when(propertyConstants.getNjurl()).thenReturn("nj-url");
        when(propertyConstants.getUrl()).thenReturn("default-url");

        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenReturn(new ResponseEntity<>("OK", HttpStatus.OK));

        when(restTemplateService.sendRequest(any(), any(), any(), any()))
                .thenReturn(new ResponseEntity<>("ok", HttpStatus.OK));

        scheduler.processFailedRequests();
        verify(jdbcTemplate).update(anyString(), eq("Success"), eq("ok"), eq(Long.parseLong(RECORD_ID)));
    }

    @Test
    public void testEnvironmentIsNull_usesDefaultUrl() {
        Map<String, Object> record = buildRecord(null, "{Authorization:Bearer}");
        when(jdbcTemplate.queryForList(anyString())).thenReturn(List.of(record));
        when(propertyConstants.getUrl()).thenReturn("default-url");

        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenReturn(new ResponseEntity<>("OK", HttpStatus.OK));

        when(restTemplateService.sendRequest(any(), any(), any(), any()))
                .thenReturn(new ResponseEntity<>("ok", HttpStatus.OK));

        scheduler.processFailedRequests();
        verify(jdbcTemplate).update(anyString(), eq("Success"), eq("ok"), eq(Long.parseLong(RECORD_ID)));
    }

    @Test
    public void testSendRequestReturns500_thenSucceedsOnRetry() {
        Map<String, Object> record = buildRecord("PRODNJ", "{Authorization:Bearer}");
        when(jdbcTemplate.queryForList(anyString())).thenReturn(List.of(record));
        when(jdbcTemplate.queryForObject(anyString(), any(), eq(String.class)))
                .thenReturn("{ENVIRONMENT:PRODNJ}");

        when(propertyConstants.getNjurl()).thenReturn("nj-url");
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenReturn(new ResponseEntity<>("OK", HttpStatus.OK));

        when(restTemplateService.sendRequest(any(), any(), any(), any()))
                .thenReturn(
                        new ResponseEntity<>("retry", HttpStatus.INTERNAL_SERVER_ERROR),
                        new ResponseEntity<>("success", HttpStatus.OK)
                );

        scheduler.processFailedRequests();

        verify(jdbcTemplate).update(anyString(), eq("Success"), eq("success"), eq(Long.parseLong(RECORD_ID)));
    }

    @Test
    public void testSendRequestFailsAllRetries_callsUpdateStatusWithFailed() {
        Map<String, Object> record = buildRecord("PRODNJ", "{Authorization:Bearer}");
        when(jdbcTemplate.queryForList(anyString())).thenReturn(List.of(record));
        when(jdbcTemplate.queryForObject(anyString(), any(), eq(String.class)))
                .thenReturn("{ENVIRONMENT:PRODNJ}");

        when(propertyConstants.getNjurl()).thenReturn("nj-url");
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenReturn(new ResponseEntity<>("OK", HttpStatus.OK));

        when(restTemplateService.sendRequest(any(), any(), any(), any()))
                .thenReturn(new ResponseEntity<>("server-error", HttpStatus.INTERNAL_SERVER_ERROR));

        scheduler.processFailedRequests();

        verify(jdbcTemplate).update(anyString(), eq("Failed"), eq("server-error"), eq(Long.parseLong(RECORD_ID)));
    }

    @Test
    public void testSendRequestThrowsException_retries_thenFails() {
        // Arrange
        Map<String, Object> record = buildRecord("PRODNJ", "{Authorization:Bearer}");
        when(jdbcTemplate.queryForList(anyString())).thenReturn(List.of(record));
        when(jdbcTemplate.queryForObject(anyString(), any(), eq(String.class)))
                .thenReturn("{environment:PRODNJ}");

        when(propertyConstants.getNjurl()).thenReturn("nj-url");
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenReturn(new ResponseEntity<>("OK", HttpStatus.OK));

        // Simulate failure for all retry attempts
        when(restTemplateService.sendRequest(any(), any(), any(), any()))
                .thenThrow(new RuntimeException("test failure"));

        // Act
        scheduler.processFailedRequests();

        // Assert: Verify that jdbcTemplate.update was called with "Failed" status
        verify(jdbcTemplate).update(
                anyString(),
                eq("Failed"),
                isNull(),
                eq(RECORD_ID)
        );
    }

    @Test
    public void testProcessHeaders_withMalformedHeader() {
        String malformed = "InvalidHeaderWithoutColon";
        Map<String, String> result = ReflectionTestUtils.invokeMethod(scheduler, "processHeaders", malformed);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetJsonElement_withValidJsonLikeString() {
        String input = "{environment:PRODNY}";
        String key = "environment";
        String result = (String) ReflectionTestUtils.invokeMethod(scheduler, "getJsonElement", input, key);

        System.out.println("Result: " + result);
        assertEquals("PRODNY", result);
    }

    @Test
    public void testGetJsonElement_withMissingKey() {
        String input = "{OTHER_KEY:VALUE}";
        String result = ReflectionTestUtils.invokeMethod(scheduler, "getJsonElement", input, "ENVIRONMENT");
        assertNull(result);
    }

    @Test
    public void testInterruptedDuringSleep_shouldBreakRetry() throws Exception {
        Thread testThread = new Thread(() -> {
            Map<String, Object> record = buildRecord("PRODNY", "{Authorization:Bearer}");
            when(jdbcTemplate.queryForList(anyString())).thenReturn(List.of(record));
            when(propertyConstants.getNyurl()).thenReturn("ny-url");

            when(restTemplate.getForEntity(anyString(), eq(String.class)))
                    .thenReturn(new ResponseEntity<>("OK", HttpStatus.OK));

            when(restTemplateService.sendRequest(any(), any(), any(), any()))
                    .thenAnswer(invocation -> {
                        Thread.currentThread().interrupt(); // simulate interruption
                        return new ResponseEntity<>("retry", HttpStatus.INTERNAL_SERVER_ERROR);
                    });

            scheduler.processFailedRequests();
        });

        testThread.start();
        testThread.join();
        verify(jdbcTemplate, never()).update(anyString(), eq("Success"), anyString(), anyLong());
    }
}
