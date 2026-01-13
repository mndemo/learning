package org.codeforamerica.shiba.output.documentfieldpreparers;

import static org.codeforamerica.shiba.output.FullNameFormatter.format;
import static org.codeforamerica.shiba.output.FullNameFormatter.getFullName;
import static org.codeforamerica.shiba.output.DocumentFieldType.SINGLE_VALUE;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.pages.data.InputData;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.springframework.stereotype.Component;

/**
 * Creates DocumentField objects for penalty warnings questions.
 * For each "yes" answer, writes the question number with selected household member names.
 * 
 * Questions (numbered 1-5):
 * 1. disqualifiedPublicAssistance
 * 2. fraudulentStatements  
 * 3. hidingFromLaw
 * 4. drugFelony
 * 5. violatingParole
 */
@Component
public class PenaltyWarningsPreparer implements DocumentFieldPreparer {

  private static final String PAGE_NAME = "penaltyWarnings";

  @Override
  public List<DocumentField> prepareDocumentFields(Application application, Document document,
      Recipient recipient) {
    PageData page = application.getApplicationData().getPagesData().getPage(PAGE_NAME);
    if (page == null || page.isEmpty()) {
      return List.of();
    }

    List<DocumentField> fields = new ArrayList<>();
    int questionNumber = 1;
    
    for (Map.Entry<String, InputData> entry : page.entrySet()) {
      InputData input = entry.getValue();
      if (input == null) {
        continue;
      }
      
      createFieldsIfAnsweredYes(application, input, entry.getKey(), questionNumber++)
          .ifPresent(fields::addAll);
    }
    return fields;
  }

  private Optional<List<DocumentField>> createFieldsIfAnsweredYes(Application application, 
      InputData input, String questionName, int questionNumber) {
    if (input == null || !input.getValue().contains("true")) {
      return Optional.empty();
    }

    List<String> memberNames = extractHouseholdMemberNames(input.getValue());
    
    // If no household members selected, use applicant name
    if (memberNames.isEmpty()) {
      String applicantName = getFullName(application);
      if (applicantName == null || applicantName.isBlank()) {
        return Optional.empty();
      }
      memberNames = List.of(applicantName);
    }

    // Create a separate DocumentField for each member
    List<DocumentField> fields = new ArrayList<>();
    for (String memberName : memberNames) {
      String value = String.format("Question %d: %s", questionNumber, memberName);
      fields.add(new DocumentField(PAGE_NAME, questionName, value, SINGLE_VALUE, null));
    }
    return Optional.of(fields);
  }

  private List<String> extractHouseholdMemberNames(List<String> values) {
    List<String> memberNames = new ArrayList<>();
    for (String value : values) {
      if (isHouseholdMemberValue(value)) {
        String formattedName = format(value);
        if (!formattedName.isBlank()) {
          memberNames.add(formattedName);
        }
      }
    }
    return memberNames;
  }

  private boolean isHouseholdMemberValue(String value) {
    return !value.equals("true") && !value.equals("false");
  }
}

