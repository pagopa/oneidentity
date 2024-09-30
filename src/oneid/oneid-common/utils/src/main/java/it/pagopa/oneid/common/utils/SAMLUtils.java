package it.pagopa.oneid.common.utils;

import it.pagopa.oneid.common.model.exception.SAMLUtilsException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import javax.xml.namespace.QName;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.security.impl.RandomIdentifierGenerationStrategy;
import net.shibboleth.utilities.java.support.xml.BasicParserPool;
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
  protected static KeyInfoGenerator keyInfoGenerator;

  static {
    secureRandomIdGenerator = new RandomIdentifierGenerationStrategy();
  }

  protected BasicParserPool basicParserPool;

  @Inject
  BasicX509Credential X509Credential;

  public SAMLUtils() {
  }

  @Inject
  public SAMLUtils(BasicParserPool basicParserPool)
      throws SAMLUtilsException {
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

    setnewKeyInfoGenerator();
  }

  public <T> T buildSAMLObject(final Class<T> clazz) {
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

  public String generateSecureRandomId() {
    return secureRandomIdGenerator.generateIdentifier();
  }

  public Signature buildSignature(SignableSAMLObject signableSAMLObject)
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

  public void setnewKeyInfoGenerator() {
    X509KeyInfoGeneratorFactory keyInfoGeneratorFactory = new X509KeyInfoGeneratorFactory();
    keyInfoGeneratorFactory.setEmitEntityCertificate(true);
    keyInfoGenerator = keyInfoGeneratorFactory.newInstance();
  }

}
