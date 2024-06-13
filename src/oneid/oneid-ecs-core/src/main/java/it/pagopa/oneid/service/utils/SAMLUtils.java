package it.pagopa.oneid.service.utils;


import jakarta.enterprise.context.ApplicationScoped;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.resolver.ResolverException;
import net.shibboleth.utilities.java.support.security.impl.RandomIdentifierGenerationStrategy;
import net.shibboleth.utilities.java.support.xml.BasicParserPool;
import net.shibboleth.utilities.java.support.xml.ParserPool;
import org.apache.commons.codec.binary.Base64;
import org.opensaml.core.config.ConfigurationService;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.core.xml.XMLObjectBuilderFactory;
import org.opensaml.core.xml.config.XMLObjectProviderRegistry;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.saml.common.SAMLObjectContentReference;
import org.opensaml.saml.common.SignableSAMLObject;
import org.opensaml.saml.saml2.core.*;
import org.opensaml.security.SecurityException;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.xmlsec.keyinfo.KeyInfoGenerator;
import org.opensaml.xmlsec.keyinfo.impl.X509KeyInfoGeneratorFactory;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.support.SignatureConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.xml.namespace.QName;
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
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class SAMLUtils {


    private static Logger logger = LoggerFactory.getLogger(SAMLUtils.class);
    public BasicX509Credential X509Credential;
    public KeyInfoGenerator keyInfoGenerator;
    private static RandomIdentifierGenerationStrategy secureRandomIdGenerator;

    static {
        secureRandomIdGenerator = new RandomIdentifierGenerationStrategy();
    }

    public SAMLUtils() throws CertificateException, IOException, NoSuchAlgorithmException, InvalidKeySpecException {

        XMLObjectProviderRegistry registry = new XMLObjectProviderRegistry();
        ConfigurationService.register(XMLObjectProviderRegistry.class, registry);
        registry.setParserPool(getParserPool());
        try {
            InitializationService.initialize();
        } catch (InitializationException e) {
            logger.error(e.getMessage(), e);
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

    private static ParserPool getParserPool() {

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
        features.put("http://apache.org/xml/features/validation/schema/normalized-value", Boolean.FALSE);
        features.put("http://javax.xml.XMLConstants/feature/secure-processing", Boolean.TRUE);

        parserPool.setBuilderFeatures(features);
        parserPool.setBuilderAttributes(new HashMap<String, Object>());

        try {
            parserPool.initialize();
        } catch (ComponentInitializationException e) {
            logger.error(e.getMessage(), e);
        }

        return parserPool;
    }

    public static String generateSecureRandomId() {
        return secureRandomIdGenerator.generateIdentifier();
    }

    public static Issuer buildIssuer() {
        Issuer issuer = buildSAMLObject(Issuer.class);
        issuer.setValue(System.getenv("METADATA_URL"));
        issuer.setNameQualifier("test");
        issuer.setFormat(NameIDType.ENTITY);

        return issuer;
    }

    public static NameIDPolicy buildNameIdPolicy() {
        NameIDPolicy nameIDPolicy = buildSAMLObject(NameIDPolicy.class);
        nameIDPolicy.setFormat(NameIDType.TRANSIENT);

        return nameIDPolicy;
    }

    public static RequestedAuthnContext buildRequestedAuthnContext() {
        RequestedAuthnContext requestedAuthnContext = buildSAMLObject(RequestedAuthnContext.class);
        requestedAuthnContext.setComparison(AuthnContextComparisonTypeEnumeration.MINIMUM);

        requestedAuthnContext.getAuthnContextClassRefs().add(buildAuthnContextClassRef());
        return requestedAuthnContext;
    }

    private static AuthnContextClassRef buildAuthnContextClassRef() {
        AuthnContextClassRef authnContextClassRef = buildSAMLObject(AuthnContextClassRef.class);
        authnContextClassRef.setURI("https://www.spid.gov.it/SpidL2");
        return authnContextClassRef;
    }



    public Signature buildSignature(SignableSAMLObject signableSAMLObject) throws SecurityException {

        Signature signature = buildSAMLObject(Signature.class);

        signature.setSigningCredential(this.X509Credential);
        signature.setSignatureAlgorithm(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA512);
        signature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
        signature.setKeyInfo(this.keyInfoGenerator.generate(this.X509Credential));

        SAMLObjectContentReference contentReference = new SAMLObjectContentReference(signableSAMLObject);
        contentReference.setDigestAlgorithm(SignatureConstants.ALGO_ID_DIGEST_SHA512);
        signature.getContentReferences().add(contentReference);

        return signature;
    }

    public void setX509Credential() throws IOException, CertificateException, NoSuchAlgorithmException, InvalidKeySpecException {
        InputStream inputStreamCert = getClass().getClassLoader().getResourceAsStream("credentials/crt.pem");

        byte[] byteCert = new byte[inputStreamCert.available()];

        inputStreamCert.read(byteCert);
        String stringCert = new String(byteCert);

        InputStream targetStream = new ByteArrayInputStream(stringCert.getBytes());
        X509Certificate cert = (X509Certificate) CertificateFactory
                .getInstance("X509")
                .generateCertificate(targetStream);


        this.X509Credential = new BasicX509Credential(cert);
        InputStream inputStreamKey = getClass().getClassLoader().getResourceAsStream("credentials/key.pem");

        byte[] byteKey = new byte[inputStreamKey.available()];
        inputStreamKey.read(byteKey);
        String stringKey = new String(byteKey);

        String privateKeyPEM = stringKey
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replaceAll(System.lineSeparator(), "")
                .replace("-----END PRIVATE KEY-----", "");

        byte[] byteKeyDecoded = Base64.decodeBase64(privateKeyPEM);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(byteKeyDecoded);

        RSAPrivateKey rsaPrivateKey = (RSAPrivateKey) KeyFactory
                .getInstance("RSA")
                .generatePrivate(keySpec);

        this.X509Credential.setPrivateKey(rsaPrivateKey);

    }

    public void setnewKeyInfoGenerator() {
        X509KeyInfoGeneratorFactory keyInfoGeneratorFactory = new X509KeyInfoGeneratorFactory();
        keyInfoGeneratorFactory.setEmitEntityCertificate(true);
        this.keyInfoGenerator = keyInfoGeneratorFactory.newInstance();
    }

}
