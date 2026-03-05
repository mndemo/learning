
package org.codeforamerica.shiba;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

import org.codeforamerica.shiba.pages.events.PageEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Adhoc tests for ResubmissionService scheduled jobs. Tagged {@code adhoc} for selective runs.
 *
 * <p>Run all jobs:
 *
 * <pre>
 * ./gradlew test --tests "org.codeforamerica.shiba.ResubmissionServiceAdhocTest" --info
 * </pre>
 *
 * <p>Run individual jobs:
 *
 * <pre>
 * ./gradlew test --tests "org.codeforamerica.shiba.ResubmissionServiceAdhocTest#resubmitFailedApplications"
 * ./gradlew test --tests "org.codeforamerica.shiba.ResubmissionServiceAdhocTest#republishApplicationsInSendingStatus"
 * ./gradlew test --tests "org.codeforamerica.shiba.ResubmissionServiceAdhocTest#resubmitBlankStatusApplicationsViaEsb"
 * </pre>
 *
 * <p>Uses test profile (H2 in-memory DB). With empty DB, jobs complete quickly logging
 * "no applications to resubmit". EmailClient and PageEventPublisher are mocked to
 * avoid sending real emails or firing ESB events. To exclude from CI: {@code -PexcludeAdhoc=true}
 * (requires gradle config).
 */
@SpringBootTest(properties = {"mnbenefits_env_url=http://localhost:8080"})
@ActiveProfiles("test")
@Tag("adhoc")
class ResubmissionServiceAdhocTest {

  @Autowired
  private ResubmissionService resubmissionService;

  @MockitoBean
  private org.codeforamerica.shiba.pages.emails.EmailClient emailClient;

  @MockitoBean
  private PageEventPublisher pageEventPublisher;

  @BeforeEach
  void setUp() {
    resubmissionService.setIsEnableEmailResubmissionTask(true);
    resubmissionService.setIsEnableEsbResubmissionTask(true);
    resubmissionService.setIsEnableNoStatusEsbResubmissionTask(true);

    // No-op stubs to prevent NPE if jobs find data
    doAnswer(inv -> null).when(emailClient).resubmitFailedEmail(any(), any(), any(), any());
    doAnswer(inv -> null).when(pageEventPublisher).publish(any());
  }

  @Test
  void resubmitFailedApplications() {
    resubmissionService.resubmitFailedApplications();
  }

  @Test
  void republishApplicationsInSendingStatus() {
    resubmissionService.republishApplicationsInSendingStatus();
  }

  @Test
  void resubmitBlankStatusApplicationsViaEsb() {
    resubmissionService.resubmitBlankStatusApplicationsViaEsb();
  }

  @Test
  void allJobsRunWithoutError() {
    resubmissionService.resubmitFailedApplications();
    resubmissionService.republishApplicationsInSendingStatus();
    resubmissionService.resubmitBlankStatusApplicationsViaEsb();
  }
}
