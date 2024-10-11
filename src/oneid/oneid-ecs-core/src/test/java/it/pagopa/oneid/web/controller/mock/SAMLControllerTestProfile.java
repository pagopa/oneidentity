package it.pagopa.oneid.web.controller.mock;

import io.quarkus.test.junit.QuarkusTestProfile;
import it.pagopa.oneid.service.MockClientProducer;
import it.pagopa.oneid.service.MockConfigBasicX509Credential;
import java.util.Set;

public class SAMLControllerTestProfile implements QuarkusTestProfile {

  @Override
  public Set<Class<?>> getEnabledAlternatives() {
    return Set.of(MockSAMLControllerSessionServiceImpl.class, MockClientProducer.class,
        MockConfigBasicX509Credential.class);
  }

}