package it.pagopa.oneid.common.utils;

import it.pagopa.oneid.common.model.exception.SAMLUtilsException;
import it.pagopa.oneid.common.utils.logging.CustomLogging;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SignatureException;
import javax.xml.namespace.QName;
import net.shibboleth.utilities.java.support.security.impl.RandomIdentifierGenerationStrategy;
import net.shibboleth.utilities.java.support.xml.BasicParserPool;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.XMLObjectBuilderFactory;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.Marshaller;
import org.opensaml.core.xml.io.MarshallerFactory;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.common.SAMLObjectContentReference;
import org.opensaml.saml.common.SignableSAMLObject;
import org.opensaml.saml.saml2.metadata.KeyDescriptor;
import org.opensaml.security.SecurityException;
import org.opensaml.security.credential.UsageType;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.xmlsec.keyinfo.KeyInfoGenerator;
import org.opensaml.xmlsec.keyinfo.impl.X509KeyInfoGeneratorFactory;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.support.SignatureConstants;
import org.w3c.dom.Element;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.zip.Deflater;

@ApplicationScoped
@CustomLogging
public class SAMLUtils {

  private static final RandomIdentifierGenerationStrategy secureRandomIdGenerator = new RandomIdentifierGenerationStrategy();
  protected BasicX509Credential basicX509Credential;
  protected BasicParserPool basicParserPool;
  protected MarshallerFactory marshallerFactory;
  private KeyInfoGenerator keyInfoGenerator;

  @Inject
  public SAMLUtils(BasicParserPool basicParserPool, BasicX509Credential basicX509Credential,
      MarshallerFactory marshallerFactory)
      throws SAMLUtilsException {
    this.basicParserPool = basicParserPool;
    this.basicX509Credential = basicX509Credential;
    this.marshallerFactory = marshallerFactory;

    setNewKeyInfoGenerator();
  }

  public SAMLUtils() {
  }

  public <T> T buildSAMLObject(Class<? extends XMLObject> clazz) {
    try {
      XMLObjectBuilderFactory builderFactory = XMLObjectProviderRegistrySupport.getBuilderFactory();
      QName defaultElementName = (QName) clazz.getDeclaredField("DEFAULT_ELEMENT_NAME").get(null);
      return (T) builderFactory.getBuilder(defaultElementName).buildObject(defaultElementName);
    } catch (IllegalAccessException | NoSuchFieldException e) {
      throw new IllegalArgumentException("Could not create SAML object", e);
    }
  }

  public String generateSecureRandomId() {
    return secureRandomIdGenerator.generateIdentifier();
  }

  public Signature buildSignature(SignableSAMLObject signableSAMLObject) throws SAMLUtilsException {
    SignableSAMLObject safeSignableSAMLObject = java.util.Objects.requireNonNull(signableSAMLObject);
    Signature signature = buildSAMLObject(Signature.class);
    signature.setSigningCredential(basicX509Credential);
    signature.setSignatureAlgorithm(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA512);
    signature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);

    try {
      signature.setKeyInfo(keyInfoGenerator.generate(basicX509Credential));
    } catch (SecurityException e) {
      throw new SAMLUtilsException(e);
    }

    SAMLObjectContentReference contentReference = new SAMLObjectContentReference(
        safeSignableSAMLObject);
    contentReference.setDigestAlgorithm(SignatureConstants.ALGO_ID_DIGEST_SHA512);
    signature.getContentReferences().add(contentReference);

    return signature;
  }

  public String encodeAuthnRequestForRedirect(org.opensaml.saml.saml2.core.AuthnRequest authnRequest)
      throws SAMLUtilsException {
    org.opensaml.saml.saml2.core.AuthnRequest safeAuthnRequest = java.util.Objects.requireNonNull(authnRequest);
    Marshaller out = marshallerFactory.getMarshaller(safeAuthnRequest);
    if (out == null) {
      throw new SAMLUtilsException();
    }

    try {
      Element element = out.marshall(safeAuthnRequest);
      String xml = getStringValue(element);
      return Base64.getEncoder().encodeToString(deflate(xml.getBytes(StandardCharsets.UTF_8)));
    } catch (MarshallingException e) {
      throw new SAMLUtilsException(e);
    }
  }

  public String buildRedirectQueryString(String samlRequest, String relayState, String sigAlg) {
    StringBuilder queryString = new StringBuilder();
    queryString.append("SAMLRequest=").append(URLEncoder.encode(samlRequest, StandardCharsets.UTF_8));
    if (relayState != null && !relayState.isBlank()) {
      queryString.append("&RelayState=").append(URLEncoder.encode(relayState,
          StandardCharsets.UTF_8));
    }
    queryString.append("&SigAlg=").append(URLEncoder.encode(sigAlg, StandardCharsets.UTF_8));
    return queryString.toString();
  }

  public String signRedirectQueryString(String queryString) throws SAMLUtilsException {
    try {
      java.security.Signature signature = java.security.Signature.getInstance("SHA512withRSA");
      PrivateKey privateKey = basicX509Credential.getPrivateKey();
      signature.initSign(privateKey);
      signature.update(queryString.getBytes(StandardCharsets.UTF_8));
      return Base64.getEncoder().encodeToString(signature.sign());
    } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
      throw new SAMLUtilsException(e);
    }
  }

  public KeyDescriptor buildKeyDescriptor() throws SAMLUtilsException {
    KeyDescriptor keyDescriptor = buildSAMLObject(KeyDescriptor.class);
    keyDescriptor.setUse(UsageType.SIGNING);

    try {
      keyDescriptor.setKeyInfo(keyInfoGenerator.generate(basicX509Credential));
    } catch (SecurityException e) {
      throw new SAMLUtilsException(e);
    }

    return keyDescriptor;
  }

  private void setNewKeyInfoGenerator() {
    X509KeyInfoGeneratorFactory keyInfoGeneratorFactory = new X509KeyInfoGeneratorFactory();
    keyInfoGeneratorFactory.setEmitEntityCertificate(true);
    keyInfoGenerator = keyInfoGeneratorFactory.newInstance();
  }

  private byte[] deflate(byte[] input) {
    Deflater deflater = new Deflater(Deflater.DEFAULT_COMPRESSION, true);
    deflater.setInput(input);
    deflater.finish();

    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      byte[] buffer = new byte[1024];
      while (!deflater.finished()) {
        int count = deflater.deflate(buffer);
        outputStream.write(buffer, 0, count);
      }
      return outputStream.toByteArray();
    } catch (java.io.IOException e) {
      throw new RuntimeException(e);
    } finally {
      deflater.end();
    }
  }

  private String getStringValue(Element element) {
    StreamResult result = new StreamResult(new StringWriter());
    try {
      Transformer transformer = TransformerFactory.newInstance().newTransformer();
      transformer.transform(new DOMSource(element), result);
    } catch (TransformerException e) {
      throw new RuntimeException(e);
    }
    return result.getWriter().toString();
  }
}
