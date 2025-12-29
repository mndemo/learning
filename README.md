package org.codeforamerica.shiba.testutilities;

import io.percy.selenium.Percy;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.Set;

public class PercyTestPage extends Page {

  protected final Percy percy;
  
  // Pages that should not be included in Percy visual snapshots
  // These are intermediate/transitional pages that users quickly pass through
  private static final Set<String> EXCLUDED_PAGES = Set.of(
      "How to add documents",
      "Upload documents"
  );

  public PercyTestPage(RemoteWebDriver driver) {
    super(driver);
    this.percy = new Percy(driver);
  }

  /**
   * Takes a Percy snapshot of the current page, but only if the page is not in the exclusion list.
   */
  private void takeSnapshotIfNotExcluded() {
    String currentPageTitle = driver.getTitle();
    if (!EXCLUDED_PAGES.contains(currentPageTitle)) {
      percy.snapshot(currentPageTitle);
    }
  }

  public void clickLink(String linkText, String nextPage) {
      takeSnapshotIfNotExcluded();
      super.clickLink(linkText, nextPage);
    }
  
  public void clickButton(String buttonText, String nextPage) {
      takeSnapshotIfNotExcluded();
      super.clickButton(buttonText, nextPage);
    }
  
  public void clickButtonLink(String buttonLinkText, String nextPage) {
      takeSnapshotIfNotExcluded();
      super.clickButtonLink(buttonLinkText, nextPage);
    }
  
  public void clickCustomButton(String buttonText, int retryCount, String nextPage) {
      takeSnapshotIfNotExcluded();
      super.clickCustomButton(buttonText, retryCount, nextPage);
    }
}
