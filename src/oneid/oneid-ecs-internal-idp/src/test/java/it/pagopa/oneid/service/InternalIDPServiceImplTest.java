package it.pagopa.oneid.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import it.pagopa.oneid.common.connector.ClientConnectorImpl;
import it.pagopa.oneid.common.model.exception.OneIdentityException;
import it.pagopa.oneid.connector.InternalIDPUsersConnectorImpl;
import it.pagopa.oneid.connector.SessionConnectorImpl;
import it.pagopa.oneid.service.mock.X509CredentialTestProfile;
import jakarta.inject.Inject;
import java.util.Base64;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.opensaml.xmlsec.signature.support.SignatureValidator;

@QuarkusTest
@TestProfile(X509CredentialTestProfile.class)
class InternalIDPServiceImplTest {

  @InjectMock
  InternalIDPUsersConnectorImpl internalIDPUsersConnectorImpl;
  @InjectMock
  ClientConnectorImpl clientConnectorImpl;
  @InjectMock
  SessionConnectorImpl sessionConnectorImpl;

  @Inject
  InternalIDPServiceImpl internalIDPServiceImpl;


  @Test
  void getAuthnRequestFromString_ok() throws OneIdentityException {
    String xml = "<samlp:AuthnRequest xmlns:samlp=\"urn:oasis:names:tc:SAML:2.0:protocol\" ID=\"_id\" Version=\"2.0\" IssueInstant=\"2020-01-01T00:00:00Z\" AssertionConsumerServiceURL=\"http://acs\"><saml:Issuer xmlns:saml=\"urn:oasis:names:tc:SAML:2.0:assertion\">issuer</saml:Issuer><samlp:NameIDPolicy AllowCreate=\"true\"/></samlp:AuthnRequest>";
    String base64 = Base64.getEncoder().encodeToString(xml.getBytes());
    assertDoesNotThrow(() -> internalIDPServiceImpl.getAuthnRequestFromString(base64));
  }

  @Test
  void getAuthnRequestFromString_shouldThrowOnInvalidBase64() {
    String invalidString = "not-base64";
    assertThrows(OneIdentityException.class,
        () -> internalIDPServiceImpl.getAuthnRequestFromString(invalidString));
  }

  @Test
  void validateAuthnRequest_ok() {
    // Build a real AuthnRequest using OpenSAML builders
    org.opensaml.saml.saml2.core.Issuer issuer = new org.opensaml.saml.saml2.core.impl.IssuerBuilder().buildObject();
    issuer.setValue("issuer");

    org.opensaml.saml.saml2.core.AuthnRequest req = new org.opensaml.saml.saml2.core.impl.AuthnRequestBuilder().buildObject();
    req.setAttributeConsumingServiceIndex(1);
    req.setIssuer(issuer);
    req.setAssertionConsumerServiceURL("http://acs");
    req.setDestination("http://acs");
    org.opensaml.xmlsec.signature.Signature signature = new org.opensaml.xmlsec.signature.impl.SignatureBuilder().buildObject();
    req.setSignature(signature);
    try (MockedStatic<SignatureValidator> mocked = org.mockito.Mockito.mockStatic(
        SignatureValidator.class)) {
      // Make the static validate method do nothing (no exception)
      mocked.when(
              () -> SignatureValidator.validate(signature, internalIDPServiceImpl.basicX509Credential))
          .then(invocation -> null);
      assertDoesNotThrow(() -> internalIDPServiceImpl.validateAuthnRequest(req));
    }
  }

  @Test
  void validateAuthnRequest_shouldThrowIfNull() {
    assertThrows(OneIdentityException.class,
        () -> internalIDPServiceImpl.validateAuthnRequest(null));
  }
}