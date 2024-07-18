package it.pagopa.oneid.service.utils;

import static it.pagopa.oneid.model.Base64SAMLResponses.CORRECT_SAML_RESPONSE_01;
import static it.pagopa.oneid.model.Base64SAMLResponses.INVALID_SIGNATURE_SAML_RESPONSE_04;
import static it.pagopa.oneid.model.Base64SAMLResponses.UNSIGNED_ASSERTION_SAML_RESPONSE_03;
import static it.pagopa.oneid.model.Base64SAMLResponses.UNSIGNED_SAML_RESPONSE_02;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import io.quarkus.test.junit.QuarkusTest;
import it.pagopa.oneid.common.model.exception.OneIdentityException;
import it.pagopa.oneid.common.utils.SAMLUtilsConstants;
import it.pagopa.oneid.exception.SAMLValidationException;
import jakarta.inject.Inject;
import lombok.SneakyThrows;
import net.shibboleth.utilities.java.support.logic.ConstraintViolationException;
import net.shibboleth.utilities.java.support.xml.XMLParserException;
import org.codehaus.plexus.util.StringUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AuthnContextComparisonTypeEnumeration;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.NameIDPolicy;
import org.opensaml.saml.saml2.core.NameIDType;
import org.opensaml.saml.saml2.core.RequestedAuthnContext;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.xmlsec.signature.support.SignatureException;

@QuarkusTest
public class SAMLUtilsExtendedCoreTest {

  private static final String IDP_URL = "https://localhost:8443";

  @Inject
  SAMLUtilsExtendedCore samlUtilsExtendedCore;


  @Test
  void buildIssuer() {
    Issuer issuer = samlUtilsExtendedCore.buildIssuer();
    assertNotNull(issuer);
    assertEquals(SAMLUtilsConstants.SERVICE_PROVIDER_URI, issuer.getValue());
    assertEquals(SAMLUtilsConstants.SERVICE_PROVIDER_URI, issuer.getNameQualifier());
    assertEquals(NameIDType.ENTITY, issuer.getFormat());
  }

  @Test
  void buildNameIdPolicy() {
    NameIDPolicy nameIDPolicy = samlUtilsExtendedCore.buildNameIdPolicy();
    assertNotNull(nameIDPolicy);
    assertEquals(NameIDType.TRANSIENT, nameIDPolicy.getFormat());
  }

  @ParameterizedTest
  @ValueSource(strings = {"spidLevelTest"})
  @NullAndEmptySource
  void buildRequestedAuthnContext(String spidLevel) {
    RequestedAuthnContext requestedAuthnContext = samlUtilsExtendedCore.buildRequestedAuthnContext(
        spidLevel);

    assertNotNull(requestedAuthnContext);
    assertEquals(AuthnContextComparisonTypeEnumeration.MINIMUM,
        requestedAuthnContext.getComparison());
    if (StringUtils.isBlank(spidLevel)) {
      assertNull(requestedAuthnContext.getAuthnContextClassRefs().getFirst().getURI());
    } else {
      assertEquals(spidLevel, requestedAuthnContext.getAuthnContextClassRefs().getFirst().getURI());
    }

  }

  @ParameterizedTest
  @ValueSource(strings = {"https://idserver.servizicie.interno.gov.it/idp/profile/SAML2/POST/SSO",
      "notExistingIdpID"})
  @EmptySource
  @SneakyThrows
  void buildDestination(String idpID) {
    switch (idpID) {
      case "":
        assertThrows(ConstraintViolationException.class, () ->
            samlUtilsExtendedCore.buildDestination(idpID));
        break;
      case "notExistingIdpID":
        assertTrue(samlUtilsExtendedCore.buildDestination(idpID).isEmpty());
        break;
      default:
        assertTrue(samlUtilsExtendedCore.buildDestination(idpID).isPresent());
        break;
    }
  }

  @ParameterizedTest
  @ValueSource(strings = {"https://idserver.servizicie.interno.gov.it/idp/profile/SAML2/POST/SSO",
      "notExistingIdpID"})
  @EmptySource
  @SneakyThrows
  void getEntityDescriptor(String id) {
    switch (id) {
      case "":
        assertThrows(ConstraintViolationException.class, () ->
            samlUtilsExtendedCore.getEntityDescriptor(id));
        break;
      case "notExistingIdpID":
        assertTrue(samlUtilsExtendedCore.getEntityDescriptor(id).isEmpty());
        break;
      default:
        assertTrue(samlUtilsExtendedCore.getEntityDescriptor(id).isPresent());
        break;
    }
  }

  @Test
  @SneakyThrows
  void getAttributeDTOListFromAssertion_withAttributeValues() {
    Response samlResponse = samlUtilsExtendedCore.getSAMLResponseFromString(
        CORRECT_SAML_RESPONSE_01);
    Assertion assertion = samlResponse.getAssertions().getFirst();

    assertNotNull(samlUtilsExtendedCore.getAttributeDTOListFromAssertion(assertion));
    assertEquals("dateOfBirth",
        samlUtilsExtendedCore.getAttributeDTOListFromAssertion(assertion).get().getFirst()
            .getAttributeName());
    assertEquals("2000-01-01",
        samlUtilsExtendedCore.getAttributeDTOListFromAssertion(assertion).get().getFirst()
            .getAttributeValue());

  }

  @Test
  void getAttributeDTOListFromAssertion_withEmptyAttributeValues() {
    Assertion assertion = mock(Assertion.class);
    assertTrue(assertion.getAttributeStatements().isEmpty());
    assertTrue(samlUtilsExtendedCore.getAttributeDTOListFromAssertion(assertion).get().isEmpty());

  }


  @Test
  @SneakyThrows
  void getSAMLResponseFromString_success() {

    String samlResponseString = CORRECT_SAML_RESPONSE_01;

    Response response = samlUtilsExtendedCore.getSAMLResponseFromString(
        samlResponseString);

    assertNotNull(response);
    assertDoesNotThrow(() -> {
      samlUtilsExtendedCore.getSAMLResponseFromString(
          samlResponseString);
    });
  }

  @Disabled
  @Test
  @SneakyThrows
  void getSAMLResponseFromString_UnmarshallingException() {
    //TODO: find correct input that triggers UnmarshallingException

    String response = "Invaldid SAML response";
    OneIdentityException exception = assertThrows(OneIdentityException.class,
        () -> samlUtilsExtendedCore.getSAMLResponseFromString(response));
    assertEquals(UnmarshallingException.class, exception.getCause().getClass());

  }

  @Test
  @SneakyThrows
  void getSAMLResponseFromString_XMLParserException() {
    String response =
        "Invaldid SAML response";

    OneIdentityException exception = assertThrows(OneIdentityException.class,
        () -> samlUtilsExtendedCore.getSAMLResponseFromString(response));
    assertEquals(XMLParserException.class, exception.getCause().getClass());
  }

  @Test
  @SneakyThrows
  void validateSignature_success() {
    Response response = samlUtilsExtendedCore.getSAMLResponseFromString(
        CORRECT_SAML_RESPONSE_01);

    assertDoesNotThrow(() -> samlUtilsExtendedCore.validateSignature(response,
        IDP_URL));

  }


  @Test
  @SneakyThrows
  void validateSignature_UsignedSamlResponseException() {
    Response response = samlUtilsExtendedCore.getSAMLResponseFromString(
        UNSIGNED_SAML_RESPONSE_02);

    SAMLValidationException exception = assertThrows(SAMLValidationException.class,
        () -> samlUtilsExtendedCore.validateSignature(response, IDP_URL));
    assertEquals(SAMLValidationException.class, exception.getClass());

  }


  @Test
  @SneakyThrows
  void validateSignature_UnsignedAssertionException() {
    Response response = samlUtilsExtendedCore.getSAMLResponseFromString(
        UNSIGNED_ASSERTION_SAML_RESPONSE_03);

    SAMLValidationException exception = assertThrows(SAMLValidationException.class,
        () -> samlUtilsExtendedCore.validateSignature(response, IDP_URL));
    assertEquals(SAMLValidationException.class, exception.getClass());
    assertEquals("Assertion signature not present", exception.getMessage());

  }


  @Test
  @SneakyThrows
  void validateSignature_AssertionSignatureValidationException() {
    Response response = samlUtilsExtendedCore.getSAMLResponseFromString(
        INVALID_SIGNATURE_SAML_RESPONSE_04);

    SAMLValidationException exception = assertThrows(SAMLValidationException.class,
        () -> samlUtilsExtendedCore.validateSignature(response, IDP_URL));
    assertEquals(SignatureException.class, exception.getCause().getClass());
    assertEquals("Signature cryptographic validation not successful",
        exception.getCause().getMessage());

  }


  @Test
  @SneakyThrows
  void validateSignature_CredentialNotFoundException() {
    String notExistingEntityID = "notExistingEntityID";

    Response response = samlUtilsExtendedCore.getSAMLResponseFromString(
        CORRECT_SAML_RESPONSE_01);

    SAMLValidationException exception = assertThrows(SAMLValidationException.class,
        () -> samlUtilsExtendedCore.validateSignature(response, notExistingEntityID));
    assertEquals(SAMLValidationException.class, exception.getClass());
    assertEquals("Credential not found for selected IDP", exception.getMessage());

  }


}
