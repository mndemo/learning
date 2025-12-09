.and(pagesData -> {
          String whoseJobIsIt = getFirstValue(pagesData, WHOSE_JOB_IS_IT);
          // For single applicants, WHOSE_JOB_IS_IT will be empty, so we treat empty as applicant's job
          return whoseJobIsIt.isEmpty() 
              || whoseJobIsIt.contains("applicant")
              || (!spouseName.isEmpty() && whoseJobIsIt.contains(spouseName));
        });



         @Override
  public List<DocumentField> prepareDocumentFields(Application application, Document document,
      Recipient recipient) {
    List<DocumentField> fields = super.prepareDocumentFields(application, document, recipient);
    
    // For single applicants, whoseJobIsItFormatted may not exist, so we need to populate it
    // with the applicant's name
    ScopedParams params = getParams(document, application);
    Subworkflow subworkflow = getGroup(application.getApplicationData(), params.group());
    
    if (subworkflow != null && !subworkflow.isEmpty()) {
      int index = 0;
      String applicantName = getFullName(application);
      
      for (Iteration iteration : subworkflow) {
        if (params.scope().test(iteration.getPagesData())) {
          final int currentIndex = index;
          // Check if whoseJobIsItFormatted exists in the fields we already created
          boolean hasWhoseJobIsItFormatted = fields.stream()
              .anyMatch(field -> field.getGroupName().equals("nonSelfEmployment_householdSelectionForIncome")
                  && field.getName().equals("whoseJobIsItFormatted")
                  && field.getIteration() != null
                  && field.getIteration().equals(currentIndex));
          
          // If it doesn't exist and applicant name is available, add it
          if (!hasWhoseJobIsItFormatted && applicantName != null && !applicantName.trim().isEmpty()) {
            fields.add(new DocumentField(
                "nonSelfEmployment_householdSelectionForIncome",
                "whoseJobIsItFormatted",
                applicantName,
                SINGLE_VALUE,
                currentIndex));
          }
          
          index++;
        }
      }
    }
    
    return fields;
    
    }
