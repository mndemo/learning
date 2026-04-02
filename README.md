package org.codeforamerica.shiba.config;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class ShibaGlobalModelAttributes {

  private final ShibaProperties shibaProperties;

  public ShibaGlobalModelAttributes(ShibaProperties shibaProperties) {
    this.shibaProperties = shibaProperties;
  }

  @ModelAttribute("shibaBuildVersion")
  public String shibaBuildVersion() {
    return shibaProperties.getBuildVersion();
  }
}
