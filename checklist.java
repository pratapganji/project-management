@Test
void testValidateAndPopulateSundry_Exception() throws IOException {

    when(file.isEmpty()).thenReturn(false);
    when(file.getOriginalFilename()).thenReturn("test.xlsx");

    try (MockedStatic<StaticDataUtils> mocked =
             Mockito.mockStatic(StaticDataUtils.class)) {

        mocked.when(() -> StaticDataUtils.populateSundryDTO(file))
              .thenThrow(new RuntimeException("Error"));

        UploadSundryResponseDTO response =
                sundryManagementService.validateAndPopulateSundry(
                        file, "JIRA-123", "user123");

        assertNotNull(response);
        assertTrue(
            response.getErrorMessageList()
                    .contains("Error processing sundry file: Error")
        );
    }
}