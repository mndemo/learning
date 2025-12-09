@Override
  public List<DocumentField> prepareDocumentFields(Application application, Document document,
      Recipient recipient) {
    List<DocumentField> fields = super.prepareDocumentFields(application, document, recipient);

      // For single applicants, whoseJobIsItFormatted may not exist because WHOSE_JOB_IS_IT is empty.
    // Populate it with the applicant's name when missing.
    String applicantName = getFullName(application);
    if (applicantName == null || applicantName.trim().isEmpty()) {
      return fields;
    }
    
    ScopedParams params = getParams(document, application);
    Subworkflow subworkflow = getGroup(application.getApplicationData(), params.group());
    if (subworkflow == null || subworkflow.isEmpty()) {
      return fields;
    }
    
    int index = 0;
    for (Iteration iteration : subworkflow) {
      if (params.scope().test(iteration.getPagesData())) {
        PagesData pagesData = iteration.getPagesData();

                boolean hasWhoseJobIsItFormatted = pagesData.getPage("householdSelectionForIncome") != null
            && pagesData.getPage("householdSelectionForIncome").get("whoseJobIsItFormatted") != null;
        
        if (!hasWhoseJobIsItFormatted) {
          fields.add(new DocumentField(
              "nonSelfEmployment_householdSelectionForIncome",
              "whoseJobIsItFormatted",
              applicantName,
              SINGLE_VALUE,
              index));
        }
        index++;
      }
    }
    
    return fields;
  }
