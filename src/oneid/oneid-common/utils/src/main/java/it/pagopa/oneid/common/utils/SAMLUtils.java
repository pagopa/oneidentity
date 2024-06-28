package it.pagopa.oneid.common.utils;

import it.pagopa.oneid.common.model.exception.SAMLUtilsException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
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
import javax.xml.namespace.QName;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.security.impl.RandomIdentifierGenerationStrategy;
import net.shibboleth.utilities.java.support.xml.BasicParserPool;
import org.apache.commons.codec.binary.Base64;
import org.opensaml.core.config.ConfigurationService;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.core.xml.XMLObjectBuilderFactory;
import org.opensaml.core.xml.config.XMLObjectProviderRegistry;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.saml.common.SAMLObjectContentReference;
import org.opensaml.saml.common.SignableSAMLObject;
import org.opensaml.security.SecurityException;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.xmlsec.keyinfo.KeyInfoGenerator;
import org.opensaml.xmlsec.keyinfo.impl.X509KeyInfoGeneratorFactory;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.support.SignatureConstants;

@ApplicationScoped
public class SAMLUtils {

  private static final RandomIdentifierGenerationStrategy secureRandomIdGenerator;
  // TODO refactor with AWS ACM
  protected static BasicX509Credential X509Credential;
  // TODO refactor with AWS ACM
  protected static KeyInfoGenerator keyInfoGenerator;

  static {
    secureRandomIdGenerator = new RandomIdentifierGenerationStrategy();
  }

  protected BasicParserPool basicParserPool;

  public SAMLUtils() {
  }

  @Inject
  public SAMLUtils(BasicParserPool basicParserPool) throws SAMLUtilsException {
    // TODO consider refactor
    this.basicParserPool = basicParserPool;
    XMLObjectProviderRegistry registry = new XMLObjectProviderRegistry();
    ConfigurationService.register(XMLObjectProviderRegistry.class, registry);
    registry.setParserPool(basicParserPool);
    try {
      basicParserPool.initialize();
    } catch (ComponentInitializationException e) {
      throw new SAMLUtilsException(e);
    }

    try {
      InitializationService.initialize();
    } catch (InitializationException e) {
      throw new SAMLUtilsException(e);
    }

    setX509Credential();
    setnewKeyInfoGenerator();
  }

  public static <T> T buildSAMLObject(final Class<T> clazz) {
    T object = null;
    try {
      XMLObjectBuilderFactory builderFactory = XMLObjectProviderRegistrySupport.getBuilderFactory();
      QName defaultElementName = (QName) clazz.getDeclaredField("DEFAULT_ELEMENT_NAME").get(null);
      object = (T) builderFactory.getBuilder(defaultElementName).buildObject(defaultElementName);
    } catch (IllegalAccessException | NoSuchFieldException e) {
      throw new IllegalArgumentException("Could not create SAML object");
    }

    return object;
  }


  public static String generateSecureRandomId() {
    return secureRandomIdGenerator.generateIdentifier();
  }

  public static Signature buildSignature(SignableSAMLObject signableSAMLObject)
      throws SAMLUtilsException {

    Signature signature = buildSAMLObject(Signature.class);

    signature.setSigningCredential(X509Credential);
    signature.setSignatureAlgorithm(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA512);
    signature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
    try {
      signature.setKeyInfo(keyInfoGenerator.generate(X509Credential));
    } catch (SecurityException e) {
      throw new SAMLUtilsException(e);
    }

    SAMLObjectContentReference contentReference = new SAMLObjectContentReference(
        signableSAMLObject);
    contentReference.setDigestAlgorithm(SignatureConstants.ALGO_ID_DIGEST_SHA512);
    signature.getContentReferences().add(contentReference);

    return signature;
  }

  public void setX509Credential() throws SAMLUtilsException {

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

  }

  public void setnewKeyInfoGenerator() {
    X509KeyInfoGeneratorFactory keyInfoGeneratorFactory = new X509KeyInfoGeneratorFactory();
    keyInfoGeneratorFactory.setEmitEntityCertificate(true);
    keyInfoGenerator = keyInfoGeneratorFactory.newInstance();
  }

}
