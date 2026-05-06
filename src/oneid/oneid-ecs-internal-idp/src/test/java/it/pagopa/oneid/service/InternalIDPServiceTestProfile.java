package it.pagopa.oneid.service;

import io.quarkus.test.junit.QuarkusTestProfile;
import java.util.Set;

public class InternalIDPServiceTestProfile implements QuarkusTestProfile {

  @Override
  public Set<Class<?>> getEnabledAlternatives() {
    return Set.of(MockConfigBasicX509Credential.class);
  }
}
