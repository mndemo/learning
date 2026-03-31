package org.codeforamerica.shiba.pages;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.util.List;
import java.util.Map;
import org.codeforamerica.shiba.testutilities.AbstractShibaMockMvcTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Navigation and form submission for {@code pastBenefit} and {@code pastBenefitDetails}
 * (CAF flow between healthcare coverage and direct deposit / EBT / social worker).
 */
public class PastBenefitNavigationTest extends AbstractShibaMockMvcTest {

  @BeforeEach
  protected void setUp() throws Exception {
    super.setUp();
    mockMvc.perform(get("/pages/identifyCountyBeforeApplying").session(session));
    postExpectingSuccess("identifyCountyBeforeApplying", "county", "Hennepin");
    postExpectingSuccess("writtenLanguage", Map.of("writtenLanguage", List.of("ENGLISH")));
    postExpectingSuccess("spokenLanguage", Map.of("spokenLanguage", List.of("ENGLISH")));
  }

  @Test
  void shouldNavigatePastBenefitYesToDetailsThenEbtInPastWhenSnapOnly() throws Exception {
    selectPrograms("SNAP");
    postExpectingRedirect("healthcareCoverage", "healthcareCoverage", "false", "pastBenefit");
    postExpectingRedirect("pastBenefit", "hasHouseholdPastBenefits", "true", "pastBenefitDetails");
    postExpectingRedirect(
        "pastBenefitDetails",
        validPastBenefitDetailsParams(),
        "ebtInPast");
  }

  @Test
  void shouldNavigatePastBenefitYesToDetailsThenDirectDepositWhenCashOnly() throws Exception {
    selectPrograms("CASH");
    postExpectingRedirect("healthcareCoverage", "healthcareCoverage", "false", "pastBenefit");
    postExpectingRedirect("pastBenefit", "hasHouseholdPastBenefits", "true", "pastBenefitDetails");
    postExpectingRedirect(
        "pastBenefitDetails",
        validPastBenefitDetailsParams(),
        "directDeposit");
  }

  @Test
  void shouldNavigatePastBenefitYesToDetailsThenSocialWorkerWhenGrhOnly() throws Exception {
    selectPrograms("GRH");
    postExpectingRedirect("healthcareCoverage", "healthcareCoverage", "false", "pastBenefit");
    postExpectingRedirect("pastBenefit", "hasHouseholdPastBenefits", "true", "pastBenefitDetails");
    postExpectingRedirect(
        "pastBenefitDetails",
        validPastBenefitDetailsParams(),
        "socialWorker");
  }

  @Test
  void shouldRejectPastBenefitDetailsWhenWhichBenefitsMissing() throws Exception {
    selectPrograms("SNAP");
    postExpectingRedirect("healthcareCoverage", "healthcareCoverage", "false", "pastBenefit");
    postExpectingRedirect("pastBenefit", "hasHouseholdPastBenefits", "true", "pastBenefitDetails");
    postExpectingFailure(
        "pastBenefitDetails",
        Map.of(
            "whenPastBenefits", List.of("NOW"),
            "wherePastBenefitsState", List.of("MN - Minnesota")));
    assertPageHasInputError("pastBenefitDetails", "whichPastBenefits");
  }

  private static Map<String, List<String>> validPastBenefitDetailsParams() {
    return Map.of(
        "whenPastBenefits", List.of("NOW"),
        "wherePastBenefitsState", List.of("MN - Minnesota"),
        "whichPastBenefits", List.of("SNAP"));
  }
}
