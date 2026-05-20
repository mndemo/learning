When the applicant never visits the militaryService page (expedited / minimum flow skips it), getFirstValue(...) returns null. Boolean.valueOf(null) → false. So the preparer writes applicantHasMilitaryService = "false" even though the question was never asked.
The fix: emit the field only when the militaryService page was actually answered. Same logic applies in the multi-household branch — if militaryService wasn't visited, skip emitting that branch's data too.
Added an early-return guard at the top of prepareDocumentFields that emits nothing when the militaryService page wasn't visited:


 List<DocumentField> results = new ArrayList<>();

  // The militaryService page is gated by skipCondition: *noOneChoseCAF? (pages-config.yaml).
  // In expedited / minimum flows where CAF is not chosen, the page is never displayed and the
  // applicant never sees the question. Emitting "false" here would falsely answer a question
  // that was never asked, so we skip the field entirely instead.
  boolean militaryServiceAsked =
      application.getApplicationData().getPagesData().getPage("militaryService") != null;
  if (!militaryServiceAsked) {
    return results;
  }


    // Expedited / minimum flow: militaryService page is skipped entirely (skipCondition
  // *noOneChoseCAF?). The preparer must NOT emit applicantHasMilitaryService, otherwise the CAF
  // shows "no" to a question that was never asked.
  @Test
  void shouldEmitNothingWhenMilitaryServicePageWasNotVisited() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPersonalInfo()
        .build();

    List<DocumentField> result = preparer.prepareDocumentFields(Application.builder()
        .applicationData(applicationData)
        .build(), null, null);

    assertThat(result).isEmpty();
  }
