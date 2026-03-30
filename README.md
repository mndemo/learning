package org.codeforamerica.shiba.output.documentfieldpreparers;

import static org.codeforamerica.shiba.output.DocumentFieldType.ENUMERATED_SINGLE_VALUE;

import java.util.ArrayList;
import java.util.List;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.springframework.stereotype.Component;

/**
 * One boolean document field per past-benefit type selected on {@code pastBenefitDetails}.
 */
@Component
public class WhichPastBenefitsPreparer implements DocumentFieldPreparer {

  private static final String PAGE_NAME = "pastBenefitDetails";
  private static final String INPUT_NAME = "whichPastBenefits";
  private static final List<String> OPTIONS = List.of(
      "CASH_ASSISTANCE", "SNAP", "TRIBAL_COMMODITIES");

  @Override
  public List<DocumentField> prepareDocumentFields(Application application, Document document,
      Recipient recipient) {
    PagesData pagesData = application.getApplicationData().getPagesData();
    if (!pagesData.containsKey(PAGE_NAME)) {
      return List.of();
    }
    List<String> selected = pagesData.safeGetPageInputValue(PAGE_NAME, INPUT_NAME);
    List<DocumentField> fields = new ArrayList<>();
    for (String option : OPTIONS) {
      fields.add(new DocumentField(PAGE_NAME, option,
          String.valueOf(selected.contains(option)),
          ENUMERATED_SINGLE_VALUE));
    }
    return fields;
  }
}
