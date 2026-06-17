package it.pagopa.oneid.service;

import it.pagopa.oneid.common.utils.config.ConfigBasicX509Credential;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Alternative;
import jakarta.inject.Singleton;
import jakarta.ws.rs.Produces;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Objects;
import org.mockito.Mockito;
import org.opensaml.security.x509.BasicX509Credential;

@Alternative
@Dependent
public class MockConfigBasicX509Credential extends ConfigBasicX509Credential {

  @Singleton
  @Produces
  BasicX509Credential mockBasicX509Credential() {
    try {
      KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
      keyPairGenerator.initialize(2048);
      KeyPair keyPair = keyPairGenerator.generateKeyPair();

      X509Certificate certificate = Mockito.mock(X509Certificate.class);
      Mockito.when(certificate.getPublicKey()).thenReturn(keyPair.getPublic());

      BasicX509Credential credential = new BasicX509Credential(certificate);
      credential.setPrivateKey(Objects.requireNonNull(keyPair.getPrivate()));
      return credential;
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }
}
