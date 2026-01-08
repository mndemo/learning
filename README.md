package org.codeforamerica.shiba.output.documentfieldpreparers;

import static org.codeforamerica.shiba.output.FullNameFormatter.format;
import static org.codeforamerica.shiba.output.DocumentFieldType.SINGLE_VALUE;

import java.util.ArrayList;
import java.util.List;
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
 * Questions (numbered 1-4):
 * 1. disqualifiedPublicAssistance
 * 2. fraudulentStatements  
 * 3. hidingFromLaw
 * 4. drugFelony
 */
@Component
public class PenaltyWarningsPreparer implements DocumentFieldPreparer {

  private static final String PAGE_NAME = "penaltyWarnings";
  private static final List<String> QUESTIONS = List.of(
      "disqualifiedPublicAssistance",
      "fraudulentStatements",
      "hidingFromLaw",
      "drugFelony"
  );

  @Override
  public List<DocumentField> prepareDocumentFields(Application application, Document document,
      Recipient recipient) {
    PageData page = application.getApplicationData().getPagesData().getPage(PAGE_NAME);
    if (page == null) {
      return List.of();
    }

    List<DocumentField> fields = new ArrayList<>();
    for (int i = 0; i < QUESTIONS.size(); i++) {
      String questionName = QUESTIONS.get(i);
      int questionNumber = i + 1;
      
      createFieldIfAnsweredYes(page, questionName, questionNumber)
          .ifPresent(fields::add);
    }
    return fields;
  }

  private Optional<DocumentField> createFieldIfAnsweredYes(PageData page, 
      String questionName, int questionNumber) {
    InputData input = page.get(questionName);
    if (input == null || !input.getValue().contains("true")) {
      return Optional.empty();
    }

    List<String> memberNames = extractHouseholdMemberNames(input.getValue());
    if (memberNames.isEmpty()) {
      return Optional.empty();
    }

    String value = String.format("Question %d: %s", questionNumber, String.join(", ", memberNames));
    return Optional.of(new DocumentField(PAGE_NAME, questionName, value, SINGLE_VALUE, null));
  }

  private List<String> extractHouseholdMemberNames(List<String> values) {
    return values.stream()
        .filter(value -> !value.equals("true") && !value.equals("false"))
        .map(format)
        .filter(name -> !name.isEmpty())
        .toList();
  }
}

