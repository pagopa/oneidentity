package it.pagopa.oneid.model.enums;

import lombok.Getter;
import org.jboss.logging.Logger.Level;

@Getter
public enum EnvironmentMapping {

  d("dev", Level.INFO),
  u("uat", Level.ERROR),
  p("prod", Level.ERROR);

  private final String envLong;
  private final Level logLevel;

  EnvironmentMapping(String envLong, Level logLevel) {
    this.envLong = envLong;
    this.logLevel = logLevel;
  }

}
