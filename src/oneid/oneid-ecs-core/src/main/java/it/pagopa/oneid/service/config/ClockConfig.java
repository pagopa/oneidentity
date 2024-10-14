package it.pagopa.oneid.service.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Produces;
import java.time.Clock;

public class ClockConfig {

  @ApplicationScoped
  @Produces
  Clock clock() {
    return Clock.systemDefaultZone();
  }
}
