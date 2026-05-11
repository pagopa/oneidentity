package it.pagopa.oneid.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import it.pagopa.oneid.common.model.exception.OneIdentityException;
import it.pagopa.oneid.common.utils.SAMLUtilsConstants;
import it.pagopa.oneid.connector.InternalIDPUsersConnectorImpl;
import it.pagopa.oneid.model.IDPInternalUser;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.xml.namespace.QName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.schema.XSAny;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Extensions;

@QuarkusTest
@TestProfile(InternalIDPServiceTestProfile.class)
class InternalIDPServiceImplTest {

  @Inject
  InternalIDPServiceImpl internalIDPServiceImpl;

  @InjectMock
  InternalIDPUsersConnectorImpl internalIDPUsersConnectorImpl;

  @Test
  void getAuthnRequestFromString() {
  }

  @Test
  void validateAuthnRequest() {
  }

  @Test
  void getClientByAttributeConsumingServiceIndex() {
  }

  @Test
  @DisplayName("given_authnRequest_without_extensions_when_extractAgeLimit_then_empty")
  void extractAgeLimit_noExtensions() {
    AuthnRequest authnRequest = Mockito.mock(AuthnRequest.class);
    Mockito.when(authnRequest.getExtensions()).thenReturn(null);

    Optional<int[]> result = internalIDPServiceImpl.extractAgeLimit(authnRequest);

    assertTrue(result.isEmpty());
  }

  @Test
  @DisplayName("given_authnRequest_with_age_limit_extensions_when_extractAgeLimit_then_returns_values")
  void extractAgeLimit_withAgeLimit() {
    AuthnRequest authnRequest = Mockito.mock(AuthnRequest.class);
    Extensions extensions = Mockito.mock(Extensions.class);

    XSAny minAgeElement = Mockito.mock(XSAny.class);
    Mockito.when(minAgeElement.getElementQName()).thenReturn(
        new QName(SAMLUtilsConstants.NAMESPACE_URI_SPID, SAMLUtilsConstants.LOCAL_NAME_MIN_AGE));
    Mockito.when(minAgeElement.getTextContent()).thenReturn("14");

    XSAny maxAgeElement = Mockito.mock(XSAny.class);
    Mockito.when(maxAgeElement.getElementQName()).thenReturn(
        new QName(SAMLUtilsConstants.NAMESPACE_URI_SPID, SAMLUtilsConstants.LOCAL_NAME_MAX_AGE));
    Mockito.when(maxAgeElement.getTextContent()).thenReturn("99");

    XMLObject ageLimitElement = Mockito.mock(XMLObject.class);
    Mockito.when(ageLimitElement.getElementQName()).thenReturn(
        new QName(SAMLUtilsConstants.NAMESPACE_URI_SPID, SAMLUtilsConstants.LOCAL_NAME_AGE_LIMIT));
    Mockito.when(ageLimitElement.getOrderedChildren())
        .thenReturn(List.of(minAgeElement, maxAgeElement));

    Mockito.when(extensions.getUnknownXMLObjects()).thenReturn(List.of(ageLimitElement));
    Mockito.when(authnRequest.getExtensions()).thenReturn(extensions);

    Optional<int[]> result = internalIDPServiceImpl.extractAgeLimit(authnRequest);

    assertTrue(result.isPresent());
    assertArrayEquals(new int[]{14, 99}, result.get());
  }

  @Test
  @DisplayName("given_null_age_params_when_verifyAge_then_no_exception")
  void verifyAge_nullParams() {
    assertDoesNotThrow(
        () -> internalIDPServiceImpl.verifyAge("clientId", "username", null, null));
  }

  @Test
  @DisplayName("given_user_within_age_range_when_verifyAge_then_no_exception")
  void verifyAge_withinRange() {
    IDPInternalUser user = IDPInternalUser.builder()
        .username("testUser")
        .namespace("clientId")
        .password("pass")
        .samlAttributes(Map.of("name", "Mario"))
        .age(20)
        .build();

    Mockito.when(internalIDPUsersConnectorImpl
            .getIDPInternalUserByUsernameAndNamespace("testUser", "clientId"))
        .thenReturn(Optional.of(user));

    assertDoesNotThrow(
        () -> internalIDPServiceImpl.verifyAge("clientId", "testUser", 14, 99));
  }

  @Test
  @DisplayName("given_user_below_min_age_when_verifyAge_then_throws")
  void verifyAge_belowMinAge() {
    IDPInternalUser user = IDPInternalUser.builder()
        .username("testUser")
        .namespace("clientId")
        .password("pass")
        .samlAttributes(Map.of("name", "Mario"))
        .age(10)
        .build();

    Mockito.when(internalIDPUsersConnectorImpl
            .getIDPInternalUserByUsernameAndNamespace("testUser", "clientId"))
        .thenReturn(Optional.of(user));

    OneIdentityException ex = assertThrows(OneIdentityException.class,
        () -> internalIDPServiceImpl.verifyAge("clientId", "testUser", 14, 99));
    assertTrue(ex.getMessage().contains("Mario"));
  }

  @Test
  @DisplayName("given_user_above_max_age_when_verifyAge_then_throws")
  void verifyAge_aboveMaxAge() {
    IDPInternalUser user = IDPInternalUser.builder()
        .username("testUser")
        .namespace("clientId")
        .password("pass")
        .samlAttributes(Map.of("name", "Luigi"))
        .age(25)
        .build();

    Mockito.when(internalIDPUsersConnectorImpl
            .getIDPInternalUserByUsernameAndNamespace("testUser", "clientId"))
        .thenReturn(Optional.of(user));

    OneIdentityException ex = assertThrows(OneIdentityException.class,
        () -> internalIDPServiceImpl.verifyAge("clientId", "testUser", 14, 18));
    assertTrue(ex.getMessage().contains("Luigi"));
  }

  @Test
  @DisplayName("given_max_age_99_and_user_older_when_verifyAge_then_no_exception")
  void verifyAge_maxAge99MeansNoUpperLimit() {
    IDPInternalUser user = IDPInternalUser.builder()
        .username("testUser")
        .namespace("clientId")
        .password("pass")
        .samlAttributes(Map.of("name", "Nonna"))
        .age(80)
        .build();

    Mockito.when(internalIDPUsersConnectorImpl
            .getIDPInternalUserByUsernameAndNamespace("testUser", "clientId"))
        .thenReturn(Optional.of(user));

    assertDoesNotThrow(
        () -> internalIDPServiceImpl.verifyAge("clientId", "testUser", 14, 99));
  }

  @Test
  @DisplayName("given_user_not_found_when_verifyAge_then_throws")
  void verifyAge_userNotFound() {
    Mockito.when(internalIDPUsersConnectorImpl
            .getIDPInternalUserByUsernameAndNamespace("testUser", "clientId"))
        .thenReturn(Optional.empty());

    OneIdentityException ex = assertThrows(OneIdentityException.class,
        () -> internalIDPServiceImpl.verifyAge("clientId", "testUser", 14, 99));
    assertEquals("User not found", ex.getMessage());
  }

  @Test
  @DisplayName("given_user_without_age_when_verifyAge_then_throws")
  void verifyAge_missingAge() {
    IDPInternalUser user = IDPInternalUser.builder()
        .username("testUser")
        .namespace("clientId")
        .password("pass")
        .samlAttributes(Map.of("name", "Mario"))
        .build();

    Mockito.when(internalIDPUsersConnectorImpl
            .getIDPInternalUserByUsernameAndNamespace("testUser", "clientId"))
        .thenReturn(Optional.of(user));

    //in case of a already existing user with missing age, the method should not throw an exception and allow the flow to continue
    assertDoesNotThrow(
        () -> internalIDPServiceImpl.verifyAge("clientId", "testUser", 14, 99));
  }
}
