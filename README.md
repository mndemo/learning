package org.codeforamerica.shiba.output.documentfieldpreparers;

import static org.codeforamerica.shiba.output.DocumentFieldType.ENUMERATED_SINGLE_VALUE;

import java.util.List;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.pages.data.PageData;
import org.springframework.stereotype.Component;

@Component
public class WhichPastBenefitsPreparer implements DocumentFieldPreparer {

  @Override
  public List<DocumentField> prepareDocumentFields(Application application, Document document,
      Recipient recipient) {
    PageData page = application.getApplicationData().getPagesData().getPage("pastBenefitDetails");
    if (page == null) {
      return List.of();
    }

    List<String> chosen =
        page.containsKey("whichPastBenefits") ? page.get("whichPastBenefits").getValue() : List.of();

    return List.of(
        boolField("CASH_ASSISTANCE", chosen),
        boolField("SNAP", chosen),
        boolField("TRIBAL_COMMODITIES", chosen));
  }

  private static DocumentField boolField(String option, List<String> chosen) {
    return new DocumentField("pastBenefitDetails", option,
        String.valueOf(chosen.contains(option)),
        ENUMERATED_SINGLE_VALUE);
  }
}
