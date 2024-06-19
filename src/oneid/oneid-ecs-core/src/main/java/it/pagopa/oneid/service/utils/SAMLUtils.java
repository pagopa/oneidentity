package it.pagopa.oneid.service.utils;


import static it.pagopa.oneid.service.utils.SAMLUtilsConstants.METADATA_URL;
import static it.pagopa.oneid.service.utils.SAMLUtilsConstants.SERVICE_PROVIDER_URI;
import it.pagopa.oneid.exception.OneIdentityException;
import it.pagopa.oneid.exception.SAMLUtilsException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.File;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.xml.namespace.QName;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import net.shibboleth.utilities.java.support.resolver.ResolverException;
import net.shibboleth.utilities.java.support.security.impl.RandomIdentifierGenerationStrategy;
import net.shibboleth.utilities.java.support.xml.BasicParserPool;
import net.shibboleth.utilities.java.support.xml.XMLParserException;
import org.apache.commons.codec.binary.Base64;
import org.opensaml.core.config.ConfigurationService;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.core.xml.XMLObjectBuilderFactory;
import org.opensaml.core.xml.config.XMLObjectProviderRegistry;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.saml.common.SAMLObjectContentReference;
import org.opensaml.saml.common.SignableSAMLObject;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.criterion.EntityRoleCriterion;
import org.opensaml.saml.criterion.ProtocolCriterion;
import org.opensaml.saml.metadata.resolver.impl.FilesystemMetadataResolver;
import org.opensaml.saml.metadata.resolver.impl.PredicateRoleDescriptorResolver;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.AuthnContextComparisonTypeEnumeration;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.NameIDPolicy;
import org.opensaml.saml.saml2.core.NameIDType;
import org.opensaml.saml.saml2.core.RequestedAuthnContext;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.security.impl.MetadataCredentialResolver;
import org.opensaml.saml.security.impl.SAMLSignatureProfileValidator;
import org.opensaml.security.SecurityException;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.credential.UsageType;
import org.opensaml.security.criteria.UsageCriterion;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.xmlsec.config.impl.DefaultSecurityConfigurationBootstrap;
import org.opensaml.xmlsec.keyinfo.KeyInfoCredentialResolver;
import org.opensaml.xmlsec.keyinfo.KeyInfoGenerator;
import org.opensaml.xmlsec.keyinfo.impl.X509KeyInfoGeneratorFactory;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.support.SignatureConstants;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.opensaml.xmlsec.signature.support.SignatureValidator;

@ApplicationScoped
public class SAMLUtils {

  // TODO consider adding a BasicParserPool parameter
  private static final RandomIdentifierGenerationStrategy secureRandomIdGenerator;

  static {
    secureRandomIdGenerator = new RandomIdentifierGenerationStrategy();
  }

  private final MetadataCredentialResolver metadataCredentialResolver;
  private final FilesystemMetadataResolver metadataResolver;

  // TODO refactor with AWS ACM
  public BasicX509Credential X509Credential;
  // TODO refactor with AWS ACM
  public KeyInfoGenerator keyInfoGenerator;


  @Inject
  public SAMLUtils() throws SAMLUtilsException {
    // TODO consider refactor
    XMLObjectProviderRegistry registry = new XMLObjectProviderRegistry();
    ConfigurationService.register(XMLObjectProviderRegistry.class, registry);
    registry.setParserPool(getBasicParserPool());
    BasicParserPool parserPool = getBasicParserPool();
    try {
      parserPool.initialize();
    } catch (ComponentInitializationException e) {
      throw new SAMLUtilsException(e);
    }

    try {
      InitializationService.initialize();
    } catch (InitializationException e) {
      throw new SAMLUtilsException(e);
    }
    //TODO: add CIE metadata resolver
    // TODO: env var fileName (DEV, UAT, PROD)
    String fileName = "metadata/spid.xml";

    try {
      metadataResolver = new FilesystemMetadataResolver(new File(fileName));
    } catch (ResolverException e) {
      throw new SAMLUtilsException(e);
    }

    metadataResolver.setId("spidMetadataResolver");
    metadataResolver.setParserPool(parserPool);
    try {
      metadataResolver.initialize();
    } catch (ComponentInitializationException e) {
      throw new SAMLUtilsException(e);
    }

    metadataCredentialResolver = new MetadataCredentialResolver();
    PredicateRoleDescriptorResolver roleResolver = new PredicateRoleDescriptorResolver(
        metadataResolver);
    KeyInfoCredentialResolver keyResolver = DefaultSecurityConfigurationBootstrap.buildBasicInlineKeyInfoCredentialResolver();
    metadataCredentialResolver.setKeyInfoCredentialResolver(keyResolver);
    metadataCredentialResolver.setRoleDescriptorResolver(roleResolver);

    try {
      metadataCredentialResolver.initialize();
      roleResolver.initialize();
    } catch (ComponentInitializationException e) {
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

  private static BasicParserPool getBasicParserPool() {
    BasicParserPool parserPool = new BasicParserPool();
    parserPool.setMaxPoolSize(100);
    parserPool.setCoalescing(true);
    parserPool.setIgnoreComments(true);
    parserPool.setIgnoreElementContentWhitespace(true);
    parserPool.setNamespaceAware(true);
    parserPool.setExpandEntityReferences(false);
    parserPool.setXincludeAware(false);

    final Map<String, Boolean> features = new HashMap<String, Boolean>();
    features.put("http://xml.org/sax/features/external-general-entities", Boolean.FALSE);
    features.put("http://xml.org/sax/features/external-parameter-entities", Boolean.FALSE);
    features.put("http://apache.org/xml/features/disallow-doctype-decl", Boolean.TRUE);
    features.put("http://apache.org/xml/features/validation/schema/normalized-value",
        Boolean.FALSE);
    features.put("http://javax.xml.XMLConstants/feature/secure-processing", Boolean.TRUE);

    parserPool.setBuilderFeatures(features);
    parserPool.setBuilderAttributes(new HashMap<String, Object>());
    return parserPool;
  }

  public static String generateSecureRandomId() {
    return secureRandomIdGenerator.generateIdentifier();
  }

  public static Issuer buildIssuer() {
    Issuer issuer = buildSAMLObject(Issuer.class);
    issuer.setValue(METADATA_URL);
    issuer.setNameQualifier(SERVICE_PROVIDER_URI);
    issuer.setFormat(NameIDType.ENTITY);

    return issuer;
  }

  public static NameIDPolicy buildNameIdPolicy() {
    NameIDPolicy nameIDPolicy = buildSAMLObject(NameIDPolicy.class);
    nameIDPolicy.setFormat(NameIDType.TRANSIENT);

    return nameIDPolicy;
  }

  public static RequestedAuthnContext buildRequestedAuthnContext(String spidLevel) {
    RequestedAuthnContext requestedAuthnContext = buildSAMLObject(RequestedAuthnContext.class);
    requestedAuthnContext.setComparison(AuthnContextComparisonTypeEnumeration.MINIMUM);
    requestedAuthnContext.getAuthnContextClassRefs().add(buildAuthnContextClassRef(spidLevel));

    return requestedAuthnContext;
  }

  private static AuthnContextClassRef buildAuthnContextClassRef(String spidLevel) {
    AuthnContextClassRef authnContextClassRef = buildSAMLObject(AuthnContextClassRef.class);
    authnContextClassRef.setURI(spidLevel);

    return authnContextClassRef;
  }

  public static Response getSAMLResponseFromString(String SAMLResponse)
      throws OneIdentityException {

    byte[] decodedSamlResponse = Base64.decodeBase64(SAMLResponse);

    try {
      return (Response) XMLObjectSupport.unmarshallFromInputStream(getBasicParserPool(),
          new ByteArrayInputStream(decodedSamlResponse));
    } catch (XMLParserException | UnmarshallingException e) {
      throw new OneIdentityException(e);
    }
  }

  public void validateSignature(Response response) throws SAMLUtilsException {
    SAMLSignatureProfileValidator profileValidator = new SAMLSignatureProfileValidator();
    Assertion assertion = response.getAssertions().getFirst();

    Optional<Credential> credential = getCredential(assertion.getIssuer().toString());

    if (credential.isPresent()) {
      // Validate 'Response' signature
      if (response.getSignature() != null) {
        try {
          profileValidator.validate(response.getSignature());
          SignatureValidator.validate(response.getSignature(), credential.get());
        } catch (SignatureException e) {
          throw new SAMLUtilsException(e);
        }
      }
      // Validate 'Assertion' signature
      if (assertion.getSignature() != null) {
        try {
          profileValidator.validate(assertion.getSignature());
          SignatureValidator.validate(assertion.getSignature(), credential.get());
        } catch (SignatureException e) {
          throw new SAMLUtilsException(e);
        }
      }
    } else {
      throw new SAMLUtilsException("Credential not found for selected IDP");
    }
  }

  public Optional<Credential> getCredential(String entityID) throws SAMLUtilsException {
    // TODO do we need to add a cache layer? (With ConcurrentHashMap)
    CriteriaSet criteriaSet = new CriteriaSet();
    criteriaSet.add(new EntityIdCriterion(entityID));
    criteriaSet.add(new UsageCriterion(UsageType.SIGNING));
    criteriaSet.add(new EntityRoleCriterion(
        new QName("urn:oasis:names:tc:SAML:2.0:metadata", "IDPSSODescriptor", "md")));
    criteriaSet.add(new ProtocolCriterion(SAMLConstants.SAML20P_NS));

    Credential credential;
    try {
      credential = metadataCredentialResolver.resolveSingle(criteriaSet);
    } catch (ResolverException e) {
      throw new SAMLUtilsException(e);
    }

    return Optional.ofNullable(credential);
  }

  public Optional<EntityDescriptor> getEntityDescriptor(String entityID) throws SAMLUtilsException {
    // TODO do we need to add a cache layer? (With ConcurrentHashMap)
    CriteriaSet criteriaSet = new CriteriaSet();
    criteriaSet.add(new EntityIdCriterion(entityID));

    EntityDescriptor entityDescriptor;
    try {
      entityDescriptor = metadataResolver.resolveSingle(criteriaSet);
    } catch (ResolverException e) {
      throw new SAMLUtilsException(e);
    }

    return Optional.ofNullable(entityDescriptor);
  }

  public Optional<String> buildDestination(String idpID) throws SAMLUtilsException {

    return getEntityDescriptor(idpID)
        .map(descriptor -> descriptor.getIDPSSODescriptor("urn:oasis:names:tc:SAML:2.0:protocol")
            .getSingleSignOnServices()
            .getFirst()
            .getLocation()
        );
  }

  public Signature buildSignature(SignableSAMLObject signableSAMLObject) throws SAMLUtilsException {

    Signature signature = buildSAMLObject(Signature.class);

    signature.setSigningCredential(this.X509Credential);
    signature.setSignatureAlgorithm(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA512);
    signature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
    try {
      signature.setKeyInfo(this.keyInfoGenerator.generate(this.X509Credential));
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

    this.X509Credential = new BasicX509Credential(cert);
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

    this.X509Credential.setPrivateKey(rsaPrivateKey);

  }

  public void setnewKeyInfoGenerator() {
    X509KeyInfoGeneratorFactory keyInfoGeneratorFactory = new X509KeyInfoGeneratorFactory();
    keyInfoGeneratorFactory.setEmitEntityCertificate(true);
    this.keyInfoGenerator = keyInfoGeneratorFactory.newInstance();
  }

}
