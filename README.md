package org.codeforamerica.shiba.output.documentfieldpreparers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.output.DocumentFieldType.SINGLE_VALUE;
import static org.codeforamerica.shiba.testutilities.TestUtils.createApplicationInputSingleValue;

import java.util.List;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.testutilities.TestApplicationDataBuilder;
import org.junit.jupiter.api.Test;

class WhichPastBenefitsPreparerTest {

  private final WhichPastBenefitsPreparer preparer = new WhichPastBenefitsPreparer();

  @Test
  void formatsWhichPastBenefitsWhenWhenAndWhereAlsoSubmitted() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData("pastBenefit", "hasHouseholdPastBenefits", List.of("true"))
        .withPageData("pastBenefitDetails", "whenPastBenefits", List.of("NOW"))
        .withPageData("pastBenefitDetails", "wherePastBenefitsState", List.of("MN - Minnesota"))
        .withPageData("pastBenefitDetails", "whichPastBenefits", List.of("SNAP"))
        .build();

    List<DocumentField> result = preparer.prepareDocumentFields(Application.builder()
        .applicationData(applicationData)
        .build(), null, null);

    assertThat(result).containsExactly(
        createApplicationInputSingleValue("pastBenefitDetails", "whichPastBenefits", "SNAP"));
  }

  @Test
  void shouldJoinMultipleSelectionsWithCommaAndSpace() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData("pastBenefitDetails", "whichPastBenefits",
            List.of("SNAP", "TRIBAL_COMMODITIES"))
        .build();

    List<DocumentField> result = preparer.prepareDocumentFields(Application.builder()
        .applicationData(applicationData)
        .build(), null, null);

    assertThat(result).containsExactly(
        createApplicationInputSingleValue("pastBenefitDetails", "whichPastBenefits", "SNAP, Food"));
  }

  @Test
  void shouldPreserveSubmissionOrder() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData("pastBenefitDetails", "whichPastBenefits",
            List.of("CASH_ASSISTANCE", "SNAP"))
        .build();

    List<DocumentField> result = preparer.prepareDocumentFields(Application.builder()
        .applicationData(applicationData)
        .build(), null, null);

    assertThat(result).containsExactly(
        createApplicationInputSingleValue("pastBenefitDetails", "whichPastBenefits", "CASH, SNAP"));
  }

  @Test
  void shouldMapSingleCashAssistanceToCash() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData("pastBenefitDetails", "whichPastBenefits", List.of("CASH_ASSISTANCE"))
        .build();

    List<DocumentField> result = preparer.prepareDocumentFields(Application.builder()
        .applicationData(applicationData)
        .build(), null, null);

    assertThat(result).containsExactly(
        createApplicationInputSingleValue("pastBenefitDetails", "whichPastBenefits", "CASH"));
  }

  @Test
  void shouldMapAllThreeOptionsWhenSelected() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData("pastBenefitDetails", "whichPastBenefits",
            List.of("CASH_ASSISTANCE", "SNAP", "TRIBAL_COMMODITIES"))
        .build();

    List<DocumentField> result = preparer.prepareDocumentFields(Application.builder()
        .applicationData(applicationData)
        .build(), null, null);

    assertThat(result).containsExactly(
        createApplicationInputSingleValue("pastBenefitDetails", "whichPastBenefits",
            "CASH, SNAP, Food"));
  }

  @Test
  void shouldIgnoreUnknownOptionValues() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData("pastBenefitDetails", "whichPastBenefits",
            List.of("UNKNOWN", "SNAP", "NOT_A_PROGRAM"))
        .build();

    List<DocumentField> result = preparer.prepareDocumentFields(Application.builder()
        .applicationData(applicationData)
        .build(), null, null);

    assertThat(result).containsExactly(
        createApplicationInputSingleValue("pastBenefitDetails", "whichPastBenefits", "SNAP"));
  }

  @Test
  void shouldMapEmptySelectionToBlankString() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData("pastBenefitDetails", "whichPastBenefits", List.of())
        .build();

    List<DocumentField> result = preparer.prepareDocumentFields(Application.builder()
        .applicationData(applicationData)
        .build(), null, null);

    assertThat(result).containsExactly(
        new DocumentField("pastBenefitDetails", "whichPastBenefits", List.of(""), SINGLE_VALUE));
  }

  @Test
  void shouldReturnBlankWhenPastBenefitDetailsPageExistsButWhichPastBenefitsMissing() {
    ApplicationData applicationData = new ApplicationData();
    applicationData.getPagesData().put("pastBenefitDetails", new PageData());

    List<DocumentField> result = preparer.prepareDocumentFields(Application.builder()
        .applicationData(applicationData)
        .build(), null, null);

    assertThat(result).containsExactly(
        new DocumentField("pastBenefitDetails", "whichPastBenefits", List.of(""), SINGLE_VALUE));
  }

  @Test
  void shouldReturnNothingWhenPastBenefitDetailsPageMissing() {
    ApplicationData applicationData = new ApplicationData();

    List<DocumentField> result = preparer.prepareDocumentFields(Application.builder()
        .applicationData(applicationData)
        .build(), null, null);

    assertThat(result).isEmpty();
  }
}
