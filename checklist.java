@Test
void testValidateAndPopulateSundry_Success() throws Exception {

    when(file.isEmpty()).thenReturn(false);
    when(file.getOriginalFilename()).thenReturn("test.xlsx");

    List<CatalogueSundry> sundryList = new ArrayList<>();
    CatalogueSundry sundry = new CatalogueSundry();
    sundry.setDimensionGroupName("Group1");
    sundry.setElementCode("Code1");
    sundry.setElementName("Name1");
    sundry.setSourceCode("Source1");
    sundry.setSourceDescription("Description1");
    sundry.setActiveFlag("Y");
    sundryList.add(sundry);

    when(schemaBrowserService.getSundryTempDropdown())
        .thenReturn(new ArrayList<>());

    when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class)))
        .thenReturn(1);

    // 🔴 THIS IS THE IMPORTANT PART
    try (MockedStatic<StaticDataUtils> mocked =
             Mockito.mockStatic(StaticDataUtils.class)) {

        mocked.when(() -> StaticDataUtils.populateSundryDTO(file))
              .thenReturn(sundryList);

        UploadSundryResponseDTO response =
            sundryManagementService.validateAndPopulateSundry(
                file, "JIRA-123", "user123");

        assertNotNull(response);
        assertNotNull(response.getValidSundryList());
        assertEquals(1, response.getValidSundryList().size());
    }
}