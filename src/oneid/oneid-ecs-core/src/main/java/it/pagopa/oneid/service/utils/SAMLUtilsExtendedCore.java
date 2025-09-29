package it.pagopa.oneid.service.utils;


import io.quarkus.logging.Log;
import it.pagopa.oneid.common.model.IDP;
import it.pagopa.oneid.common.model.exception.OneIdentityException;
import it.pagopa.oneid.common.model.exception.SAMLUtilsException;
import it.pagopa.oneid.common.model.exception.enums.ErrorCode;
import it.pagopa.oneid.common.utils.SAMLUtils;
import it.pagopa.oneid.common.utils.logging.CustomLogging;
import it.pagopa.oneid.exception.GenericAuthnRequestCreationException;
import it.pagopa.oneid.exception.SAMLValidationException;
import it.pagopa.oneid.model.dto.AttributeDTO;
import it.pagopa.oneid.service.config.SAMLNamespaceContext;
import it.pagopa.oneid.web.controller.interceptors.CurrentAuthDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import net.shibboleth.utilities.java.support.xml.BasicParserPool;
import net.shibboleth.utilities.java.support.xml.XMLParserException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.io.Marshaller;
import org.opensaml.core.xml.io.MarshallerFactory;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.opensaml.core.xml.schema.XSString;
import org.opensaml.core.xml.schema.impl.XSAnyImpl;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.AuthnContextComparisonTypeEnumeration;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.NameIDPolicy;
import org.opensaml.saml.saml2.core.NameIDType;
import org.opensaml.saml.saml2.core.RequestedAuthnContext;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.opensaml.xmlsec.signature.support.SignatureValidator;
import org.opensaml.xmlsec.signature.support.Signer;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

@ApplicationScoped
@CustomLogging
public class SAMLUtilsExtendedCore extends SAMLUtils {

  @ConfigProperty(name = "entity_id")
  String ENTITY_ID;

  @Inject
  CurrentAuthDTO currentAuthDTO;


  @Inject
  public SAMLUtilsExtendedCore(BasicParserPool basicParserPool,
      BasicX509Credential basicX509Credential, MarshallerFactory marshallerFactory)
      throws SAMLUtilsException {
    super(basicParserPool, basicX509Credential, marshallerFactory);
  }

  public Issuer buildIssuer() {
    return createIssuer(ENTITY_ID);
  }

  public NameIDPolicy buildNameIdPolicy() {
    return createNameIDPolicy();
  }

  public RequestedAuthnContext buildRequestedAuthnContext(String spidLevel) {
    return createRequestedAuthnContext(spidLevel, AuthnContextComparisonTypeEnumeration.MINIMUM);
  }

  public Optional<List<AttributeDTO>> getAttributeDTOListFromAssertion(Assertion assertion) {
    List<AttributeDTO> attributes = new ArrayList<>();
    assertion.getAttributeStatements().forEach(attributeStatement ->
        attributeStatement.getAttributes().forEach(attribute ->
            addAttributeDTO(attribute, attributes)
        )
    );
    return attributes.isEmpty() ? Optional.empty() : Optional.of(attributes);
  }

  public Response getSAMLResponseFromString(String SAMLResponse) throws OneIdentityException {
    byte[] decodedSamlResponse = decodeBase64(SAMLResponse);
    return unmarshallResponse(decodedSamlResponse);
  }

  public void validateSignature(Response response, IDP idp)
      throws SAMLUtilsException {
    List<Credential> credentials = getCredentials(idp.getCertificates(),
        response.getIssueInstant());
    try {
      validateResponseSignature(response, credentials);
    } catch (SignatureException e) {
      Log.error(
          ErrorCode.IDP_ERROR_RESPONSE_INVALID_SIGNATURE.getErrorMessage() + ": "
              + e.getMessage());
      throw new SAMLValidationException(ErrorCode.IDP_ERROR_RESPONSE_INVALID_SIGNATURE);
    }
    try {
      validateAssertionSignature(response.getAssertions().getFirst(), credentials);
    } catch (SignatureException e) {
      Log.error(
          ErrorCode.IDP_ERROR_ASSERTION_INVALID_SIGNATURE.getErrorMessage() + ": "
              + e.getMessage());
      throw new SAMLValidationException(ErrorCode.IDP_ERROR_ASSERTION_INVALID_SIGNATURE);
    }

  }

  public void marshallAndSign(AuthnRequest authnRequest, Signature signature) {
    Marshaller out = marshallerFactory
        .getMarshaller(authnRequest);
    if (out == null) {
      throw new GenericAuthnRequestCreationException();
    }
    try {
      out.marshall(authnRequest);
      Signer.signObject(signature);
    } catch (SignatureException | MarshallingException e) {
      throw new GenericAuthnRequestCreationException(e);
    }
  }

  // Helper Methods
  private Issuer createIssuer(String uri) {
    Issuer issuer = buildSAMLObject(Issuer.class);
    issuer.setValue(uri);
    issuer.setNameQualifier(uri);
    issuer.setFormat(NameIDType.ENTITY);
    return issuer;
  }

  private NameIDPolicy createNameIDPolicy() {
    NameIDPolicy nameIDPolicy = buildSAMLObject(NameIDPolicy.class);
    nameIDPolicy.setFormat(NameIDType.TRANSIENT);
    return nameIDPolicy;
  }

  private RequestedAuthnContext createRequestedAuthnContext(String spidLevel,
      AuthnContextComparisonTypeEnumeration comparisonType) {
    RequestedAuthnContext requestedAuthnContext = buildSAMLObject(RequestedAuthnContext.class);
    requestedAuthnContext.setComparison(comparisonType);
    requestedAuthnContext.getAuthnContextClassRefs().add(buildAuthnContextClassRef(spidLevel));
    return requestedAuthnContext;
  }

  private void addAttributeDTO(Attribute attribute, List<AttributeDTO> attributes) {
    List<XMLObject> attributeValues = attribute.getAttributeValues();
    if (!attributeValues.isEmpty()) {
      attributes.add(
          new AttributeDTO(attribute.getName(), getAttributeValue(attributeValues.getFirst())));
    }
  }

  private byte[] decodeBase64(String SAMLResponse) throws OneIdentityException {
    try {
      return Base64.getDecoder().decode(SAMLResponse);
    } catch (IllegalArgumentException e) {
      Log.error("Base64 decoding error: " + e.getMessage());
      throw new OneIdentityException(e);
    }
  }

  private Response unmarshallResponse(byte[] decodedSamlResponse) throws OneIdentityException {
    // log size of SAMLResponse for possible future check on max size
    Log.info("SAMLResponse size: " + decodedSamlResponse.length);

    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    try {
      // Parse XML
      dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      dbf.setXIncludeAware(false);
      dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      dbf.setNamespaceAware(true);

      DocumentBuilder db = dbf.newDocumentBuilder();
      Document doc = db.parse(new ByteArrayInputStream(decodedSamlResponse));

      checkNoMultipleSignatures(doc);

    } catch (SAXException | IOException | ParserConfigurationException |
             XPathExpressionException e) {
      Log.error("XML parsing exception " + e.getMessage());
      // TODO uncomment once feature is active
      // throw new OneIdentityException(e);
    }

    try {
      // return response even if it has multiple signatures, the error will be handled inside SAMLController
      return (Response) XMLObjectSupport.unmarshallFromInputStream(basicParserPool,
          new ByteArrayInputStream(decodedSamlResponse));
    } catch (XMLParserException | UnmarshallingException e) {
      Log.error("Unmarshalling error: " + e.getMessage());
      throw new OneIdentityException(e);
    }
  }

  private void checkNoMultipleSignatures(Document doc) throws XPathExpressionException {
    XPath xPath = XPathFactory.newInstance().newXPath();
    // Set the namespace context to handle prefixes like 'saml2p', 'saml2', and 'ds'
    xPath.setNamespaceContext(new SAMLNamespaceContext());

    //In case of "Advice" field inside the Response, we do not consider the signatures inside it
    String expression = "count(.//ds:Signature[not(ancestor::saml2:Advice)])";
    Double responseSignatureCount = (Double) xPath.compile(expression)
        .evaluate(doc, XPathConstants.NUMBER);

    if (responseSignatureCount > 2) {
      // set a flag in currentAuthDTO to handle the error in SAMLController
      Log.error(ErrorCode.IDP_ERROR_MULTIPLE_SAMLRESPONSE_SIGNATURES_PRESENT.getErrorMessage());
      currentAuthDTO.setResponseWithMultipleSignatures(true);
    }
  }


  private void validateResponseSignature(Response response, List<Credential> credentials)
      throws SignatureException {
    if (response.getSignature() == null) {
      Log.error(ErrorCode.IDP_ERROR_RESPONSE_SIGNATURE_NOT_PRESENT.getErrorMessage());
      throw new SAMLValidationException(ErrorCode.IDP_ERROR_RESPONSE_SIGNATURE_NOT_PRESENT);
    }
    validateSignatureWithCredentials(response.getSignature(), credentials);
  }

  private void validateAssertionSignature(Assertion assertion, List<Credential> credentials)
      throws SAMLValidationException, SignatureException {
    if (assertion.getSignature() == null) {
      Log.error(ErrorCode.IDP_ERROR_ASSERTION_SIGNATURE_NOT_PRESENT.getErrorMessage());
      throw new SAMLValidationException(ErrorCode.IDP_ERROR_ASSERTION_SIGNATURE_NOT_PRESENT);
    }
    validateSignatureWithCredentials(assertion.getSignature(), credentials);
  }

  private void validateSignatureWithCredentials(Signature signature, List<Credential> credentials)
      throws SignatureException {
    for (Credential credential : credentials) {
      try {
        SignatureValidator.validate(signature, credential);
        return;
      } catch (SignatureException ignored) {
      }
    }
    throw new SignatureException("Invalid signature");
  }

  private ArrayList<Credential> getCredentials(Set<String> certificates, Instant issueInstant) {
    ArrayList<Credential> credentials = new ArrayList<>();

    for (String certificate : certificates) {

      byte[] encodedCert = Base64.getMimeDecoder().decode(certificate);
      ByteArrayInputStream inputStream = new ByteArrayInputStream(encodedCert);

      CertificateFactory certFactory = null;
      try {
        certFactory = CertificateFactory.getInstance("X.509");
      } catch (CertificateException e) {
        throw new RuntimeException(e);
      }
      X509Certificate cert = null;
      try {
        cert = (X509Certificate) certFactory.generateCertificate(inputStream);
      } catch (CertificateException e) {
        throw new RuntimeException(e);
      }
      try {
        cert.checkValidity(Date.from(issueInstant));
        credentials.add(new BasicX509Credential(cert));
      } catch (CertificateExpiredException expiredEx) {
        Log.debug("certificate expired: " + expiredEx.getMessage());
      } catch (CertificateNotYetValidException notYetValidEx) {
        Log.debug("certificate not valid yet: " + notYetValidEx.getMessage());
      }
    }
    return credentials;
  }


  private AuthnContextClassRef buildAuthnContextClassRef(String spidLevel) {
    AuthnContextClassRef authnContextClassRef = buildSAMLObject(AuthnContextClassRef.class);
    authnContextClassRef.setURI(spidLevel);

    return authnContextClassRef;
  }

  private String getAttributeValue(XMLObject attributeValue) {

    return attributeValue == null ?
        null :
        attributeValue instanceof XSString ?
            getStringAttributeValue((XSString) attributeValue) :
            attributeValue instanceof XSAnyImpl ?
                getAnyAttributeValue((XSAnyImpl) attributeValue) :
                attributeValue.toString();
  }

  private String getStringAttributeValue(XSString attributeValue) {
    return attributeValue.getValue();
  }

  private String getAnyAttributeValue(XSAnyImpl attributeValue) {
    return attributeValue.getTextContent();
  }
}
