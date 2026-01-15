package org.codeforamerica.shiba.output.documentfieldpreparers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.output.DocumentFieldType.SINGLE_VALUE;

import java.util.List;
import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.InputData;
import org.codeforamerica.shiba.testutilities.TestApplicationDataBuilder;
import org.junit.jupiter.api.Test;

class PenaltyWarningsPreparerTest {

  private final PenaltyWarningsPreparer preparer = new PenaltyWarningsPreparer();

  @Test
  void shouldReturnEmptyListWhenPageIsNull() {
    ApplicationData applicationData = new ApplicationData();
    Application application = Application.builder()
        .applicationData(applicationData)
        .build();

    List<DocumentField> result = preparer.prepareDocumentFields(application, null, null);

    assertThat(result).isEmpty();
  }

  @Test
  void shouldReturnEmptyListWhenPageIsEmpty() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData("penaltyWarnings", "dummy", List.of())
        .build();
    // Remove the dummy data to make page empty
    applicationData.getPagesData().getPage("penaltyWarnings").clear();

    Application application = Application.builder()
        .applicationData(applicationData)
        .build();

    List<DocumentField> result = preparer.prepareDocumentFields(application, null, null);

    assertThat(result).isEmpty();
  }

  @Test
  void shouldReturnEmptyListWhenAllAnswersAreNo() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData("penaltyWarnings", "disqualifiedPublicAssistance", List.of("false"))
        .withPageData("penaltyWarnings", "fraudulentStatements", List.of("false"))
        .withPageData("penaltyWarnings", "hidingFromLaw", List.of("false"))
        .withPageData("penaltyWarnings", "drugFelony", List.of("false"))
        .withPageData("penaltyWarnings", "violatingParole", List.of("false"))
        .build();

    Application application = Application.builder()
        .applicationData(applicationData)
        .build();

    List<DocumentField> result = preparer.prepareDocumentFields(application, null, null);

    assertThat(result).isEmpty();
  }

  @Test
  void shouldCreateFieldForApplicantWhenYesAndNoHouseholdMembersSelected() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData("personalInfo", "firstName", "John")
        .withPageData("personalInfo", "lastName", "Doe")
        .withPageData("penaltyWarnings", "disqualifiedPublicAssistance", List.of("true"))
        .build();

    Application application = Application.builder()
        .applicationData(applicationData)
        .build();

    List<DocumentField> result = preparer.prepareDocumentFields(application, null, null);

    assertThat(result).containsExactly(
        new DocumentField("penaltyWarnings", "disqualifiedPublicAssistance",
            "Question 1: John Doe", SINGLE_VALUE, null)
    );
  }

  @Test
  void shouldCreateFieldForEachSelectedHouseholdMember() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData("penaltyWarnings", "fraudulentStatements",
            List.of("true", "Jane Smith applicant", "Bob Johnson uuid-123"))
        .build();

    Application application = Application.builder()
        .applicationData(applicationData)
        .build();

    List<DocumentField> result = preparer.prepareDocumentFields(application, null, null);

    assertThat(result).containsExactlyInAnyOrder(
        new DocumentField("penaltyWarnings", "fraudulentStatements",
            "Question 2: Jane Smith", SINGLE_VALUE, null),
        new DocumentField("penaltyWarnings", "fraudulentStatements",
            "Question 2: Bob Johnson", SINGLE_VALUE, null)
    );
  }

  @Test
  void shouldCreateFieldsForMultipleQuestionsAnsweredYes() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData("personalInfo", "firstName", "John")
        .withPageData("personalInfo", "lastName", "Doe")
        .withPageData("penaltyWarnings", "disqualifiedPublicAssistance", List.of("true"))
        .withPageData("penaltyWarnings", "hidingFromLaw",
            List.of("true", "Jane Smith uuid-456"))
        .withPageData("penaltyWarnings", "drugFelony",
            List.of("true", "Bob Johnson uuid-789", "Alice Brown uuid-101"))
        .build();

    Application application = Application.builder()
        .applicationData(applicationData)
        .build();

    List<DocumentField> result = preparer.prepareDocumentFields(application, null, null);

    assertThat(result).containsExactlyInAnyOrder(
        new DocumentField("penaltyWarnings", "disqualifiedPublicAssistance",
            "Question 1: John Doe", SINGLE_VALUE, null),
        new DocumentField("penaltyWarnings", "hidingFromLaw",
            "Question 3: Jane Smith", SINGLE_VALUE, null),
        new DocumentField("penaltyWarnings", "drugFelony",
            "Question 4: Bob Johnson", SINGLE_VALUE, null),
        new DocumentField("penaltyWarnings", "drugFelony",
            "Question 4: Alice Brown", SINGLE_VALUE, null)
    );
  }

  @Test
  void shouldUseCorrectQuestionNumbers() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData("personalInfo", "firstName", "John")
        .withPageData("personalInfo", "lastName", "Doe")
        .withPageData("penaltyWarnings", "disqualifiedPublicAssistance", List.of("true"))
        .withPageData("penaltyWarnings", "fraudulentStatements", List.of("true"))
        .withPageData("penaltyWarnings", "hidingFromLaw", List.of("true"))
        .withPageData("penaltyWarnings", "drugFelony", List.of("true"))
        .withPageData("penaltyWarnings", "violatingParole", List.of("true"))
        .build();

    Application application = Application.builder()
        .applicationData(applicationData)
        .build();

    List<DocumentField> result = preparer.prepareDocumentFields(application, null, null);

    assertThat(result).hasSize(5);
    assertThat(result).extracting(DocumentField::getValue)
        .containsExactlyInAnyOrder(
            "Question 1: John Doe",
            "Question 2: John Doe",
            "Question 3: John Doe",
            "Question 4: John Doe",
            "Question 5: John Doe"
        );
  }

  @Test
  void shouldSkipNullInputs() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData("personalInfo", "firstName", "John")
        .withPageData("personalInfo", "lastName", "Doe")
        .withPageData("penaltyWarnings", "disqualifiedPublicAssistance", List.of("true"))
        // fraudulentStatements is missing (null)
        .withPageData("penaltyWarnings", "hidingFromLaw", List.of("true"))
        .build();

    Application application = Application.builder()
        .applicationData(applicationData)
        .build();

    List<DocumentField> result = preparer.prepareDocumentFields(application, null, null);

    assertThat(result).containsExactlyInAnyOrder(
        new DocumentField("penaltyWarnings", "disqualifiedPublicAssistance",
            "Question 1: John Doe", SINGLE_VALUE, null),
        new DocumentField("penaltyWarnings", "hidingFromLaw",
            "Question 3: John Doe", SINGLE_VALUE, null)
    );
  }

  @Test
  void shouldHandleMixedYesAndNoAnswers() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData("personalInfo", "firstName", "John")
        .withPageData("personalInfo", "lastName", "Doe")
        .withPageData("penaltyWarnings", "disqualifiedPublicAssistance", List.of("true"))
        .withPageData("penaltyWarnings", "fraudulentStatements", List.of("false"))
        .withPageData("penaltyWarnings", "hidingFromLaw", List.of("true"))
        .withPageData("penaltyWarnings", "drugFelony", List.of("false"))
        .withPageData("penaltyWarnings", "violatingParole", List.of("false"))
        .build();

    Application application = Application.builder()
        .applicationData(applicationData)
        .build();

    List<DocumentField> result = preparer.prepareDocumentFields(application, null, null);

    assertThat(result).containsExactlyInAnyOrder(
        new DocumentField("penaltyWarnings", "disqualifiedPublicAssistance",
            "Question 1: John Doe", SINGLE_VALUE, null),
        new DocumentField("penaltyWarnings", "hidingFromLaw",
            "Question 3: John Doe", SINGLE_VALUE, null)
    );
  }

  @Test
  void shouldReturnEmptyListWhenApplicantNameIsMissing() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData("penaltyWarnings", "disqualifiedPublicAssistance", List.of("true"))
        // No personalInfo page, so applicant name is null
        .build();

    Application application = Application.builder()
        .applicationData(applicationData)
        .build();

    List<DocumentField> result = preparer.prepareDocumentFields(application, null, null);

    assertThat(result).isEmpty();
  }

  @Test
  void shouldReturnEmptyListWhenApplicantNameIsBlank() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData("personalInfo", "firstName", "")
        .withPageData("personalInfo", "lastName", "")
        .withPageData("penaltyWarnings", "disqualifiedPublicAssistance", List.of("true"))
        .build();

    Application application = Application.builder()
        .applicationData(applicationData)
        .build();

    List<DocumentField> result = preparer.prepareDocumentFields(application, null, null);

    assertThat(result).isEmpty();
  }

  @Test
  void shouldFormatHouseholdMemberNamesCorrectly() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData("penaltyWarnings", "fraudulentStatements",
            List.of("true", "First Middle Last applicant", "Another Name Here uuid-abc"))
        .build();

    Application application = Application.builder()
        .applicationData(applicationData)
        .build();

    List<DocumentField> result = preparer.prepareDocumentFields(application, null, null);

    assertThat(result).containsExactlyInAnyOrder(
        new DocumentField("penaltyWarnings", "fraudulentStatements",
            "Question 2: First Middle Last", SINGLE_VALUE, null),
        new DocumentField("penaltyWarnings", "fraudulentStatements",
            "Question 2: Another Name Here", SINGLE_VALUE, null)
    );
  }

  @Test
  void shouldIgnoreFalseValuesInInputData() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData("personalInfo", "firstName", "John")
        .withPageData("personalInfo", "lastName", "Doe")
        .withPageData("penaltyWarnings", "disqualifiedPublicAssistance",
            List.of("true", "false", "Jane Smith uuid-123"))
        .build();

    Application application = Application.builder()
        .applicationData(applicationData)
        .build();

    List<DocumentField> result = preparer.prepareDocumentFields(application, null, null);

    assertThat(result).containsExactly(
        new DocumentField("penaltyWarnings", "disqualifiedPublicAssistance",
            "Question 1: Jane Smith", SINGLE_VALUE, null)
    );
  }
}

