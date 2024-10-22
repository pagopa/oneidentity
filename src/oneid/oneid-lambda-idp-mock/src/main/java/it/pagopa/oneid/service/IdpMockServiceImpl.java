package it.pagopa.oneid.service;

import io.quarkus.logging.Log;
import it.pagopa.oneid.common.model.enums.AuthLevel;
import it.pagopa.oneid.common.model.exception.OneIdentityException;
import it.pagopa.oneid.common.model.exception.SAMLUtilsException;
import it.pagopa.oneid.common.utils.SAMLUtils;
import it.pagopa.oneid.common.utils.SAMLUtilsConstants;
import it.pagopa.oneid.common.utils.logging.CustomLogging;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import net.shibboleth.utilities.java.support.xml.BasicParserPool;
import net.shibboleth.utilities.java.support.xml.XMLParserException;
import org.opensaml.core.xml.io.Marshaller;
import org.opensaml.core.xml.io.MarshallerFactory;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.saml.common.SAMLVersion;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.AttributeValue;
import org.opensaml.saml.saml2.core.Audience;
import org.opensaml.saml.saml2.core.AudienceRestriction;
import org.opensaml.saml.saml2.core.AuthnContext;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.core.Conditions;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.NameIDType;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.Status;
import org.opensaml.saml.saml2.core.StatusCode;
import org.opensaml.saml.saml2.core.Subject;
import org.opensaml.saml.saml2.core.SubjectConfirmation;
import org.opensaml.saml.saml2.core.SubjectConfirmationData;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.opensaml.xmlsec.signature.support.Signer;
import org.w3c.dom.Element;

@ApplicationScoped
@CustomLogging
public class IdpMockServiceImpl extends SAMLUtils implements IdpMockService {

  private final Clock clock;
  MarshallerFactory marshallerFactory;

  @Inject
  public IdpMockServiceImpl(BasicParserPool basicParserPool,
      BasicX509Credential basicX509Credential, MarshallerFactory marshallerFactory
      , Clock clock) throws SAMLUtilsException {
    super(basicParserPool, basicX509Credential);
    this.marshallerFactory = marshallerFactory;
    this.clock = clock;
    this.basicParserPool = basicParserPool;
  }

  @Override
  public Response createSamlResponse(AuthnRequest authnRequest) throws SAMLUtilsException {

    Response samlResponse = buildSAMLObject(Response.class);

    samlResponse.setID(generateSecureRandomId());
    samlResponse.setVersion(SAMLVersion.VERSION_20);
    samlResponse.setIssueInstant(Instant.now(clock));
    samlResponse.setInResponseTo(authnRequest.getID());
    samlResponse.setDestination(authnRequest.getAssertionConsumerServiceURL());

    //region Issuer
    Issuer issuer = buildSAMLObject(Issuer.class);
    issuer.setValue(authnRequest.getIssuer().getValue());
    issuer.setNameQualifier(authnRequest.getIssuer().getNameQualifier());
    issuer.setFormat(NameIDType.ENTITY);
    samlResponse.setIssuer(issuer);
    //endregion

    //region Status
    Status status = buildSAMLObject(Status.class);
    StatusCode statusCode = buildSAMLObject(StatusCode.class);
    statusCode.setValue(StatusCode.SUCCESS);
    status.setStatusCode(statusCode);
    samlResponse.setStatus(status);
    //endregion

    // Start of Assertion
    Issuer issuerAssertion = buildSAMLObject(Issuer.class);
    issuerAssertion.setValue(authnRequest.getIssuer().getValue());
    issuerAssertion.setNameQualifier(authnRequest.getIssuer().getNameQualifier());
    issuerAssertion.setFormat(NameIDType.ENTITY);

    Assertion assertion = buildSAMLObject(Assertion.class);
    assertion.setID(generateSecureRandomId());
    assertion.setVersion(SAMLVersion.VERSION_20);
    assertion.setIssueInstant(Instant.now(clock));
    assertion.setIssuer(issuerAssertion);

    //region Subject
    Subject subject = buildSAMLObject(Subject.class);
    NameID nameID = buildSAMLObject(NameID.class);
    nameID.setFormat(NameIDType.TRANSIENT);
    nameID.setNameQualifier(generateSecureRandomId());
    subject.setNameID(nameID);

    SubjectConfirmation subjectConfirmation = buildSAMLObject(SubjectConfirmation.class);
    SubjectConfirmationData subjectConfirmationData = buildSAMLObject(
        SubjectConfirmationData.class);
    subjectConfirmationData.setRecipient(SAMLUtilsConstants.ACS_URL);
    subjectConfirmationData.setInResponseTo(authnRequest.getID());
    subjectConfirmationData.setNotOnOrAfter(Instant.now(clock).plusSeconds(120));
    subjectConfirmation.setMethod("urn:oasis:names:tc:SAML:2.0:cm:bearer");
    subjectConfirmation.setSubjectConfirmationData(subjectConfirmationData);

    assertion.setSubject(subject);
    //endregion

    //region Conditions
    Conditions conditions = buildSAMLObject(Conditions.class);
    conditions.setNotBefore(samlResponse.getIssueInstant());
    conditions.setNotOnOrAfter(subjectConfirmationData.getNotOnOrAfter());

    AudienceRestriction audienceRestriction = buildSAMLObject(AudienceRestriction.class);
    Audience audience = buildSAMLObject(Audience.class);
    audience.setURI(SAMLUtilsConstants.SERVICE_PROVIDER_URI);
    audienceRestriction.getAudiences().add(audience);
    conditions.getAudienceRestrictions().add(audienceRestriction);

    assertion.setConditions(conditions);
    //endregion

    //region AuthnStatement
    AuthnStatement authnStatement = buildSAMLObject(AuthnStatement.class);
    authnStatement.setAuthnInstant(samlResponse.getIssueInstant());
    authnStatement.setSessionIndex(generateSecureRandomId());

    AuthnContext authnContext = buildSAMLObject(AuthnContext.class);
    AuthnContextClassRef authnContextClassRef = buildSAMLObject(
        AuthnContextClassRef.class);
    authnContextClassRef.setURI(AuthLevel.L2.getValue());
    //endregion

    //region AttributeStatements
    AttributeStatement attributeStatement = buildSAMLObject(AttributeStatement.class);

    Attribute attributeSpidCode = buildSAMLObject(Attribute.class);
    attributeSpidCode.setName("spidCode");
    attributeSpidCode.setNameFormat(SAMLUtilsConstants.NAME_FORMAT);
    AttributeValue attributeValueSpidCode = buildSAMLObject(AttributeValue.class);
    attributeValueSpidCode.setTextContent("AGID-001");

    attributeSpidCode.getAttributeValues().add(attributeValueSpidCode);

    Attribute attributeFiscalNumber = buildSAMLObject(Attribute.class);
    attributeFiscalNumber.setName("fiscalNumber");
    attributeFiscalNumber.setNameFormat(SAMLUtilsConstants.NAME_FORMAT);
    AttributeValue attributeValueFiscalNumber = buildSAMLObject(AttributeValue.class);
    attributeValueFiscalNumber.setTextContent("TINIT-GDASDV00A01H501J");

    attributeFiscalNumber.getAttributeValues().add(attributeValueFiscalNumber);

    attributeStatement.getAttributes().add(attributeSpidCode);
    attributeStatement.getAttributes().add(attributeFiscalNumber);

    assertion.getAttributeStatements().add(attributeStatement);
    //endregion

    //region Signature
    Signature signatureSamlResponse = buildSignature(samlResponse);
    Signature signatureSamlAssertion = buildSignature(assertion);
    samlResponse.setSignature(signatureSamlResponse);
    assertion.setSignature(signatureSamlAssertion);
    //endregion

    marshallAndSignAssertion(assertion, signatureSamlAssertion);

    samlResponse.getAssertions().add(assertion);

    marshallAndSignResponse(samlResponse, signatureSamlResponse);

    return samlResponse;
  }

  @Override
  public AuthnRequest getAuthnRequestFromString(String authnRequest) throws OneIdentityException {
    byte[] decodedAuthnRequest = decodeBase64(authnRequest);
    return unmarshallAuthnRequest(decodedAuthnRequest);
  }

  @Override
  public String getStringValue(Element element) {
    StreamResult result = new StreamResult(new StringWriter());
    try {
      TransformerFactory
          .newInstance()
          .newTransformer()
          .transform(new DOMSource(element), result);
    } catch (TransformerException e) {
      throw new RuntimeException(e);
    }
    return result.getWriter().toString();
  }

  @Override
  public Element getElementValueFromSamlResponse(Response samlResponse) {
    Marshaller out = marshallerFactory.getMarshaller(samlResponse);

    Element plaintextElement = null;
    try {
      plaintextElement = out.marshall(samlResponse);
    } catch (MarshallingException e) {
      throw new RuntimeException(e);
    }

    return plaintextElement;
  }

  //region private
  private byte[] decodeBase64(String authnRequest) throws OneIdentityException {
    try {
      return Base64.getDecoder().decode(authnRequest);
    } catch (IllegalArgumentException e) {
      Log.error("Base64 decoding error: " + e.getMessage());
      throw new OneIdentityException(e);
    }
  }

  private AuthnRequest unmarshallAuthnRequest(byte[] decodedAuthnRequest)
      throws OneIdentityException {
    try {
      return (AuthnRequest) XMLObjectSupport.unmarshallFromInputStream(basicParserPool,
          new ByteArrayInputStream(decodedAuthnRequest));
    } catch (XMLParserException | UnmarshallingException e) {
      Log.error("Unmarshalling error: " + e.getMessage());
      throw new OneIdentityException(e);
    }
  }

  private void marshallAndSignResponse(Response samlResponse, Signature signature) {
    Marshaller out = marshallerFactory
        .getMarshaller(samlResponse);
    if (out == null) {
      throw new RuntimeException();
    }
    try {
      out.marshall(samlResponse);
      Signer.signObject(signature);
    } catch (SignatureException | MarshallingException e) {
      throw new RuntimeException();
    }
  }

  private void marshallAndSignAssertion(Assertion samlAssertion, Signature signature) {
    Marshaller out = marshallerFactory
        .getMarshaller(samlAssertion);
    if (out == null) {
      throw new RuntimeException();
    }
    try {
      out.marshall(samlAssertion);
      Signer.signObject(signature);
    } catch (SignatureException | MarshallingException e) {
      throw new RuntimeException();
    }
  }
  //endregion
}
