package it.pagopa.oneid.service;

import it.pagopa.oneid.common.model.exception.SAMLUtilsException;
import it.pagopa.oneid.common.utils.producers.BasicX509CredentialProducer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Alternative;
import jakarta.ws.rs.Produces;
import java.io.ByteArrayInputStream;
import java.io.IOException;
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
import org.opensaml.security.x509.BasicX509Credential;

@Alternative
@Dependent
public class MockBasicX509CredentialProducer extends BasicX509CredentialProducer {

  @ApplicationScoped
  @Produces
  BasicX509Credential basicX509Credential() throws SAMLUtilsException {

    BasicX509Credential X509Credential = null;

    InputStream inputStreamCert = getClass().getClassLoader()
        .getResourceAsStream("credentials/crt.pem");

    byte[] byteCert = null;
    try {
      byteCert = new byte[inputStreamCert.available()];
    } catch (IOException e) {
      throw new SAMLUtilsException(e);
    }

    try {
      inputStreamCert.read(byteCert);
    } catch (IOException e) {
      throw new SAMLUtilsException(e);

    }
    String stringCert = new String(byteCert);

    InputStream targetStream = new ByteArrayInputStream(stringCert.getBytes());
    X509Certificate cert = null;
    try {
      cert = (X509Certificate) CertificateFactory
          .getInstance("X509")
          .generateCertificate(targetStream);
    } catch (CertificateException e) {
      throw new SAMLUtilsException(e);

    }

    X509Credential = new BasicX509Credential(cert);
    InputStream inputStreamKey = getClass().getClassLoader()
        .getResourceAsStream("credentials/key.pem");

    byte[] byteKey = null;
    try {
      byteKey = new byte[inputStreamKey.available()];
      inputStreamKey.read(byteKey);
    } catch (IOException e) {
      throw new SAMLUtilsException(e);
    }
    String stringKey = new String(byteKey);

    String privateKeyPEM = stringKey
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
      throw new SAMLUtilsException(e);
    }

    X509Credential.setPrivateKey(rsaPrivateKey);

    return X509Credential;
  }

}
