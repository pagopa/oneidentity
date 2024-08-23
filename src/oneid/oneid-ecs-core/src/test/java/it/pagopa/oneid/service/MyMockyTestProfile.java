package it.pagopa.oneid.service;

import io.quarkus.test.junit.QuarkusTestProfile;
import java.util.Set;

/**
 * Defines a 'test profile'. Tests run under a test profile will have different configuration
 * options to other tests.
 */
public class MyMockyTestProfile implements QuarkusTestProfile {

  @Override
  public Set<Class<?>> getEnabledAlternatives() {
    return Set.of(MockSessionConnectorImpl.class);
  }
}
