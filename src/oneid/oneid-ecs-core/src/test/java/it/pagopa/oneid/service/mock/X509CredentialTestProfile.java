package it.pagopa.oneid.service.mock;

import io.quarkus.test.junit.QuarkusTestProfile;
import it.pagopa.oneid.service.MockConfigBasicX509Credential;
import java.util.Set;

public class X509CredentialTestProfile implements QuarkusTestProfile {

  @Override
  public Set<Class<?>> getEnabledAlternatives() {
    return Set.of(MockConfigBasicX509Credential.class);
  }
}
