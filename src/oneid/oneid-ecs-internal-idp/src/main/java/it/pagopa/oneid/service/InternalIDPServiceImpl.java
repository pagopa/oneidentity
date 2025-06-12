package it.pagopa.oneid.service;

import io.quarkus.logging.Log;
import it.pagopa.oneid.common.connector.ClientConnectorImpl;
import it.pagopa.oneid.common.model.Client;
import it.pagopa.oneid.common.model.enums.AuthLevel;
import it.pagopa.oneid.common.model.exception.OneIdentityException;
import it.pagopa.oneid.common.model.exception.SAMLUtilsException;
import it.pagopa.oneid.common.utils.SAMLUtils;
import it.pagopa.oneid.common.utils.SAMLUtilsConstants;
import it.pagopa.oneid.common.utils.logging.CustomLogging;
import it.pagopa.oneid.connector.InternalIDPUsersConnectorImpl;
import it.pagopa.oneid.model.IDPInternalUser;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.util.Base64;
import java.util.Set;
import net.shibboleth.utilities.java.support.xml.BasicParserPool;
import net.shibboleth.utilities.java.support.xml.XMLParserException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
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

@ApplicationScoped
@CustomLogging
public class InternalIDPServiceImpl extends SAMLUtils implements InternalIDPService {

  @Inject
  ClientConnectorImpl clientConnectorImpl;
  @Inject
  InternalIDPUsersConnectorImpl internalIDPUsersConnectorImpl;

  @ConfigProperty(name = "acs_endpoint")
  String ACS_ENDPOINT;

  @ConfigProperty(name = "issuer")
  String ISSUER;

  @ConfigProperty(name = "sp_entity_id")
  String SP_ENTITY_ID;

  @Inject
  public InternalIDPServiceImpl(BasicParserPool basicParserPool,
      BasicX509Credential basicX509Credential, MarshallerFactory marshallerFactory)
      throws SAMLUtilsException {
    super(basicParserPool, basicX509Credential, marshallerFactory);
  }

  @Override
  public AuthnRequest getAuthnRequestFromString(String authnRequest) throws OneIdentityException {
    byte[] decodedAuthnRequest = decodeBase64(authnRequest);
    return unmarshallAuthnRequest(decodedAuthnRequest);
  }

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


  @Override
  public AuthnRequest validateAuthnRequest(AuthnRequest authnRequest) throws OneIdentityException {

    //todo validate clientRegistration attributes of authnRequest

    //todo validate spid attributes of authnRequest

    return null;
  }

  @Override
  public Response createSuccessfulSamlResponse(String authnRequestId,
      String clientId, String username) throws SAMLUtilsException {

    Client client = clientConnectorImpl.getClientById(clientId)
        .orElseThrow(() -> new RuntimeException("Client not found"));

    Set<String> requestedParameters = client.getRequestedParameters();

    // Retrieve the user attributes from the internal IDP users connector

    IDPInternalUser user = internalIDPUsersConnectorImpl.getIDPInternalUserByUsernameAndNamespace(
            username, clientId)
        .orElseThrow(() -> new RuntimeException("User not found"));

    return buildSAMLResponse(authnRequestId, StatusCode.SUCCESS, user, requestedParameters);

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

  private Response buildSAMLResponse(String authnRequestId, String statusCodeResponse,
      IDPInternalUser user,
      Set<String> requestedParameters)
      throws SAMLUtilsException {
    Response samlResponse = buildSAMLObject(Response.class);

    samlResponse.setID(generateSecureRandomId());
    samlResponse.setVersion(SAMLVersion.VERSION_20);
    samlResponse.setIssueInstant(Instant.now());
    samlResponse.setInResponseTo(authnRequestId);

    samlResponse.setDestination(ACS_ENDPOINT);

    //region Issuer
    Issuer issuer = buildSAMLObject(Issuer.class);
    issuer.setValue(ISSUER);
    issuer.setNameQualifier(
        ISSUER);
    issuer.setFormat(NameIDType.ENTITY);
    samlResponse.setIssuer(issuer);
    //endregion

    //region Status
    Status status = buildSAMLObject(Status.class);
    StatusCode statusCode = buildSAMLObject(StatusCode.class);
    statusCode.setValue(statusCodeResponse);
    status.setStatusCode(statusCode);
    samlResponse.setStatus(status);
    //endregion

    // Start of Assertion
    Issuer issuerAssertion = buildSAMLObject(Issuer.class);

    issuerAssertion.setValue(
        ISSUER);
    issuerAssertion.setNameQualifier(
        ISSUER);
    issuerAssertion.setFormat(NameIDType.ENTITY);

    Assertion assertion = buildSAMLObject(Assertion.class);
    assertion.setID(generateSecureRandomId());
    assertion.setVersion(SAMLVersion.VERSION_20);
    assertion.setIssueInstant(Instant.now());
    assertion.setIssuer(issuerAssertion);

    //region Subject
    Subject subject = buildSAMLObject(Subject.class);
    NameID nameID = buildSAMLObject(NameID.class);
    nameID.setFormat(NameIDType.TRANSIENT);
    nameID.setNameQualifier(generateSecureRandomId());
    nameID.setValue("test");
    subject.setNameID(nameID);

    SubjectConfirmation subjectConfirmation = buildSAMLObject(SubjectConfirmation.class);
    SubjectConfirmationData subjectConfirmationData = buildSAMLObject(
        SubjectConfirmationData.class);
    subjectConfirmationData.setRecipient(ACS_ENDPOINT);
    subjectConfirmationData.setInResponseTo(authnRequestId);
    subjectConfirmationData.setNotOnOrAfter(Instant.now().plusSeconds(120));
    subjectConfirmation.setMethod("urn:oasis:names:tc:SAML:2.0:cm:bearer");
    subjectConfirmation.setSubjectConfirmationData(subjectConfirmationData);

    subject.getSubjectConfirmations().add(subjectConfirmation);
    assertion.setSubject(subject);
    //endregion

    //region Conditions
    Conditions conditions = buildSAMLObject(Conditions.class);
    conditions.setNotBefore(samlResponse.getIssueInstant());
    conditions.setNotOnOrAfter(subjectConfirmationData.getNotOnOrAfter());

    AudienceRestriction audienceRestriction = buildSAMLObject(AudienceRestriction.class);
    Audience audience = buildSAMLObject(Audience.class);
    audience.setURI(SP_ENTITY_ID);
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
    authnContext.setAuthnContextClassRef(authnContextClassRef);
    authnStatement.setAuthnContext(authnContext);
    assertion.getAuthnStatements().add(authnStatement);
    //endregion

    // TODO handle the case where the user does not have any attributes when the SAML Response is not a successful one.
    //region AttributeStatements
    AttributeStatement attributeStatement = buildSAMLObject(AttributeStatement.class);

    // Add attributes based on the requested parameters
    for (String requestedParameter : requestedParameters) {
      Attribute attribute = buildSAMLObject(Attribute.class);
      attribute.setName(requestedParameter);
      attribute.setNameFormat(SAMLUtilsConstants.NAME_FORMAT);
      AttributeValue attributeValue = buildSAMLObject(AttributeValue.class);

      attributeValue.setTextContent(
          user.getSamlAttributes().get(requestedParameter));

      attribute.getAttributeValues().add(attributeValue);
      attributeStatement.getAttributes().add(attribute);
    }

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

}
