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
import it.pagopa.oneid.exception.ClientNotFoundException;
import it.pagopa.oneid.exception.MalformedAuthnRequestException;
import it.pagopa.oneid.exception.SAMLValidationException;
import it.pagopa.oneid.model.IDPInternalUser;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Set;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import net.shibboleth.utilities.java.support.xml.BasicParserPool;
import net.shibboleth.utilities.java.support.xml.XMLParserException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.opensaml.core.xml.io.Marshaller;
import org.opensaml.core.xml.io.MarshallerFactory;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.saml.common.SAMLObjectContentReference;
import org.opensaml.saml.common.SAMLVersion;
import org.opensaml.saml.common.SignableSAMLObject;
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
import org.opensaml.security.SecurityException;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.xmlsec.keyinfo.KeyInfoGenerator;
import org.opensaml.xmlsec.keyinfo.impl.X509KeyInfoGeneratorFactory;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.support.SignatureConstants;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.opensaml.xmlsec.signature.support.SignatureValidator;
import org.opensaml.xmlsec.signature.support.Signer;
import org.w3c.dom.Element;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParameterResponse;

@ApplicationScoped
@CustomLogging
public class InternalIDPServiceImpl extends SAMLUtils implements InternalIDPService {

  @Inject
  BasicX509Credential basicX509Credential;

  @Inject
  ClientConnectorImpl clientConnectorImpl;

  @Inject
  SsmClient ssmClient;

  @Inject
  InternalIDPUsersConnectorImpl internalIDPUsersConnectorImpl;

  @ConfigProperty(name = "acs_endpoint")
  String ACS_ENDPOINT;

  @ConfigProperty(name = "issuer")
  String ISSUER;

  @ConfigProperty(name = "sp_entity_id")
  String SP_ENTITY_ID;

  @ConfigProperty(name = "idp_internal_certificate_name")
  String idpInternalCertName;

  @ConfigProperty(name = "idp_internal_key_name")
  String idpInternalCertKeyName;

  private KeyInfoGenerator keyInfoGenerator;

  @Inject
  public InternalIDPServiceImpl(BasicParserPool basicParserPool,
      BasicX509Credential basicX509Credential, MarshallerFactory marshallerFactory)
      throws SAMLUtilsException {
    super(basicParserPool, basicX509Credential, marshallerFactory);

    setNewKeyInfoGenerator();

  }

  @Override
  public AuthnRequest getAuthnRequestFromString(String authnRequest) {
    byte[] decodedAuthnRequest = decodeBase64(authnRequest);
    return unmarshallAuthnRequest(decodedAuthnRequest);
  }

  private byte[] decodeBase64(String authnRequest) {
    try {
      return Base64.getDecoder().decode(authnRequest);
    } catch (IllegalArgumentException e) {
      Log.error("Base64 decoding error: " + e.getMessage());
      throw new MalformedAuthnRequestException(
          "Malformed AuthnRequest: Base64 decoding failed.");
    }
  }

  private AuthnRequest unmarshallAuthnRequest(byte[] decodedAuthnRequest) {
    try {
      return (AuthnRequest) XMLObjectSupport.unmarshallFromInputStream(basicParserPool,
          new ByteArrayInputStream(decodedAuthnRequest));
    } catch (XMLParserException | UnmarshallingException e) {
      Log.error("Unmarshalling error: " + e.getMessage());
      throw new MalformedAuthnRequestException(
          "Malformed AuthnRequest: Unmarshalling failed. ");
    }
  }

  @Override
  public void validateAuthnRequest(AuthnRequest authnRequest) {
    // validation of AuthnRequest fields
    if (authnRequest == null) {
      throw new SAMLValidationException("AuthnRequest is null");
    }
    // Validate AttibuteConsumingServiceIndex
    if (authnRequest.getAttributeConsumingServiceIndex() == null) {
      throw new SAMLValidationException(
          "AuthnRequest AttributeConsumingServiceIndex is missing or empty");
    }
    // Validate Issuer
    if (authnRequest.getIssuer() == null || authnRequest.getIssuer().getValue() == null
        || authnRequest.getIssuer().getValue().isEmpty()) {
      throw new SAMLValidationException("AuthnRequest Issuer is missing or empty");
    }
    // Validate Destination
    if (authnRequest.getDestination() == null || authnRequest.getDestination().isEmpty()) {
      throw new SAMLValidationException("AuthnRequest Destination is missing or empty");
    }
    // Validate Signature
    if (authnRequest.getSignature() == null) {
      throw new SAMLValidationException("AuthnRequest Signature is missing");
    }
    try {
      SignatureValidator.validate(authnRequest.getSignature(), basicX509Credential);
    } catch (SignatureException e) {
      throw new SAMLValidationException("AuthnRequest Signature is invalid");
    }


  }

  public Client getClientByAttributeConsumingServiceIndex(AuthnRequest authnRequest) {
    return clientConnectorImpl.getClientByAttributeConsumingServiceIndex(
            authnRequest.getAttributeConsumingServiceIndex())
        .orElseThrow(() -> new ClientNotFoundException(
            "Client not found for AttributeConsumingServiceIndex: "
                + authnRequest.getAttributeConsumingServiceIndex()));

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

  public void validateUserInformation(String clientId, String username, String password)
      throws OneIdentityException {
    IDPInternalUser user = internalIDPUsersConnectorImpl
        .getIDPInternalUserByUsernameAndNamespace(username, clientId)
        .orElseThrow(() -> new OneIdentityException("User not found"));

    if (!password.equals(user.getPassword())) {
      throw new OneIdentityException("Invalid password for user: " + username);
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

  @Override
  public Element getElementValueFromSamlResponse(Response samlResponse) {
    Marshaller out = marshallerFactory.getMarshaller(samlResponse);

    Element plaintextElement = null;
    try {
      plaintextElement = out.marshall(samlResponse);
    } catch (MarshallingException | NullPointerException e) {
      throw new RuntimeException(e);
    }

    return plaintextElement;
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

    // TODO: Set the NameID value to a unique identifier for the user
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
    BasicX509Credential idpX509Credential = getIdpbasicX509Credential(ssmClient);

    Signature signatureSamlResponse = buildSignature(samlResponse, idpX509Credential);
    Signature signatureSamlAssertion = buildSignature(assertion, idpX509Credential);
    samlResponse.setSignature(signatureSamlResponse);
    assertion.setSignature(signatureSamlAssertion);
    //endregion

    marshallAndSignAssertion(assertion, signatureSamlAssertion);

    samlResponse.getAssertions().add(assertion);

    marshallAndSignResponse(samlResponse, signatureSamlResponse);

    return samlResponse;
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

  public Signature buildSignature(SignableSAMLObject signableSAMLObject,
      BasicX509Credential idpX509Credential) throws SAMLUtilsException {
    Signature signature = buildSAMLObject(Signature.class);
    signature.setSigningCredential(idpX509Credential);
    signature.setSignatureAlgorithm(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA512);
    signature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);

    try {
      signature.setKeyInfo(keyInfoGenerator.generate(idpX509Credential));
    } catch (SecurityException e) {
      throw new SAMLUtilsException(e);
    }

    SAMLObjectContentReference contentReference = new SAMLObjectContentReference(
        signableSAMLObject);
    contentReference.setDigestAlgorithm(SignatureConstants.ALGO_ID_DIGEST_SHA512);
    signature.getContentReferences().add(contentReference);

    return signature;
  }

  private void setNewKeyInfoGenerator() {
    X509KeyInfoGeneratorFactory keyInfoGeneratorFactory = new X509KeyInfoGeneratorFactory();
    keyInfoGeneratorFactory.setEmitEntityCertificate(true);
    keyInfoGenerator = keyInfoGeneratorFactory.newInstance();
  }

  private BasicX509Credential getIdpbasicX509Credential(SsmClient ssmClient) {

    BasicX509Credential basicX509Credential = null;

    // region certificate
    String cert = getParameter(ssmClient, idpInternalCertName);

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
    String key = getParameter(ssmClient, idpInternalCertKeyName);

    String privateKeyPEM = key
        .replace("-----BEGIN PRIVATE KEY-----", "")
        .replaceAll(System.lineSeparator(), "")
        .replace("-----END PRIVATE KEY-----", "");

    byte[] byteKeyDecoded = org.apache.commons.codec.binary.Base64.decodeBase64(privateKeyPEM);
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

  private String getParameter(SsmClient ssmClient, String parameterName) {
    GetParameterRequest request = GetParameterRequest.builder()
        .name(parameterName)
        .withDecryption(true)  // Set to true if the parameter is encrypted
        .build();

    GetParameterResponse response = ssmClient.getParameter(request);
    return response.parameter().value();
  }

}
