package it.pagopa.oneid.common.utils.config;

import io.quarkus.logging.Log;
import jakarta.inject.Singleton;
import jakarta.ws.rs.Produces;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.opensaml.security.x509.BasicX509Credential;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParameterResponse;

@Singleton
public class ConfigBasicX509Credential {

  @ConfigProperty(name = "certificate_name")
  String certName;

  @ConfigProperty(name = "key_name")
  String keyName;

  @Singleton
  @Produces
  BasicX509Credential basicX509Credential(SsmClient ssmClient) {

    BasicX509Credential basicX509Credential = null;

    // region certificate
    String cert = getParameter(ssmClient, certName);

    InputStream targetStream = new ByteArrayInputStream(cert.getBytes());
    X509Certificate x509Cert = null;
    try {
      x509Cert = (X509Certificate) CertificateFactory
          .getInstance("X509")
          .generateCertificate(targetStream);
    } catch (CertificateException e) {
      Log.error("failed to generate certificate: " + ExceptionUtils.getStackTrace(e));
      throw new RuntimeException();
    }

    // endregion

    // region key
    String key = getParameter(ssmClient, keyName);

    String privateKeyPEM = key
        .replace("-----BEGIN PRIVATE KEY-----", "")
        .replaceAll(System.lineSeparator(), "")
        .replace("-----END PRIVATE KEY-----", "");

    byte[] byteKeyDecoded = Base64.decodeBase64(privateKeyPEM);
    PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(byteKeyDecoded);

    RSAPrivateKey rsaPrivateKey = null;
    try {
      rsaPrivateKey = (RSAPrivateKey) KeyFactory
          .getInstance("RSA")
          .generatePrivate(keySpec);
    } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
      Log.error("failed to generate private key: " + ExceptionUtils.getStackTrace(e));
      throw new RuntimeException();
    }
    // endregion

    basicX509Credential = new BasicX509Credential(x509Cert);
    basicX509Credential.setPrivateKey(rsaPrivateKey);

    return basicX509Credential;
  }

  // TODO: remove this method and use a common utility class
  private String getParameter(SsmClient ssmClient, String parameterName) {
    GetParameterRequest request = GetParameterRequest.builder()
        .name(parameterName)
        .withDecryption(true)  // Set to true if the parameter is encrypted
        .build();

    GetParameterResponse response = ssmClient.getParameter(request);
    return response.parameter().value();
  }
}
