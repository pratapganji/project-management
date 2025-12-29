import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.MockedStatic;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SundryManagementServiceImplTest {

    @InjectMocks
    private SundryManagementServiceImpl sundryManagementService;

    @Mock private CatalogueSundryRepository catalogueSundryRepository;
    @Mock private CatalogueSundryTempRepository catalogueSundryTempRepository;
    @Mock private SchemaBrowserService schemaBrowserService; // IMPORTANT: interface
    @Mock private JdbcTemplate jdbcTemplate;
    @Mock private MultipartFile file;

    @Test
    void testValidateAndPopulateSundry_InvalidUserId() {
        // No static mocking needed here because your code returns early on blank userId
        UploadSundryResponseDTO response =
                sundryManagementService.validateAndPopulateSundry(file, "JIRA-123", "");

        assertNotNull(response);
        assertTrue(response.getErrorMessageList().contains(StaticDataManagementConstants.REQUESTOR_ID_MISSING));
    }

    @Test
    void testValidateAndPopulateSundry_Exception() throws Exception {
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("test.xlsx");

        try (MockedStatic<StaticDataUtils> staticDataUtilsMock = mockStatic(StaticDataUtils.class)) {
            staticDataUtilsMock
                .when(() -> StaticDataUtils.populateSundryDTO(file))
                .thenThrow(new RuntimeException("Error"));

            UploadSundryResponseDTO response =
                    sundryManagementService.validateAndPopulateSundry(file, "JIRA-123", "user123");

            assertNotNull(response);
            assertTrue(response.getErrorMessageList().get(0).contains("Error processing sundry file"));
        }
    }
}