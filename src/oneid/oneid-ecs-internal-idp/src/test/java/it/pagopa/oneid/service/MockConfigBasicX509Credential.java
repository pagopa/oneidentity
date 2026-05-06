package it.pagopa.oneid.service;

import it.pagopa.oneid.common.utils.config.ConfigBasicX509Credential;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Alternative;
import jakarta.inject.Singleton;
import jakarta.ws.rs.Produces;
import org.mockito.Mockito;
import org.opensaml.security.x509.BasicX509Credential;

@Alternative
@Dependent
public class MockConfigBasicX509Credential extends ConfigBasicX509Credential {

  @Singleton
  @Produces
  BasicX509Credential basicX509Credential() {
    return Mockito.mock(BasicX509Credential.class);
  }
}
