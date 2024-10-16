package it.pagopa.oneid.web.controller.mock;

import io.quarkus.test.junit.QuarkusTestProfile;
import it.pagopa.oneid.service.MockClientProducer;
import it.pagopa.oneid.service.MockConfigBasicX509Credential;
import java.util.Set;

public class OIDCControllerTestProfile implements QuarkusTestProfile {

  @Override
  public Set<Class<?>> getEnabledAlternatives() {
    return Set.of(MockOIDCControllerSessionServiceImpl.class, MockClientProducer.class,
        MockConfigBasicX509Credential.class);
  }
}


