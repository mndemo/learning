     .map(pageData -> Optional.ofNullable(pageData.get(formInput.getName()))
            .map(inputData -> inputData.errorMessageKeys(pageData))
            .orElse(List.of()))



			 public boolean satisfies(PageData pageData) {
    if (pageData == null || pageData.isEmpty() || input == null) return false;
    InputData inputData = pageData.get(input);
    if (inputData == null || inputData.getValue() == null) return false;
    return matcher.matches(inputData.getValue(), value);
  }


    /**
   * Evaluates this condition with optional application context. Used when the condition
   * depends on application-level data (e.g. HOUSEHOLD_HAS_MEMBERS). If applicationData
   * is null or the condition is not a custom condition, delegates to {@link #satisfies(PageData)}.
   */
  public boolean satisfies(PageData pageData, ApplicationData applicationData) {
    if (applicationData == null || customCondition == null) {
      return satisfies(pageData);
    }
    if ("HOUSEHOLD_HAS_MEMBERS".equals(customCondition)) {
      return applicationData.getApplicantAndHouseholdMemberSize() > 1;
    }
    return satisfies(pageData);
  }



     if (pageData == null || pageData.isEmpty() || cond == null || cond.getInput() == null) return false;
    var inputData = pageData.get(cond.getInput());
    if (inputData == null || inputData.getValue() == null) return false;
    return cond.getMatcher().matches(inputData.getValue(), cond.getValue());
