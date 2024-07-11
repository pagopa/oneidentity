package it.pagopa.oneid.service.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import io.quarkus.test.junit.QuarkusTest;
import it.pagopa.oneid.common.utils.SAMLUtilsConstants;
import jakarta.inject.Inject;
import lombok.SneakyThrows;
import net.shibboleth.utilities.java.support.logic.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.opensaml.saml.saml2.core.AuthnContextComparisonTypeEnumeration;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.NameIDPolicy;
import org.opensaml.saml.saml2.core.NameIDType;
import org.opensaml.saml.saml2.core.RequestedAuthnContext;

@QuarkusTest
public class SAMLUtilsExtendedCoreTest {

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
  @ValueSource(strings = {"l1", "l2", "l3"})
  @NullAndEmptySource
  void buildRequestedAuthnContext(String spidLevel) {
    RequestedAuthnContext requestedAuthnContext = samlUtilsExtendedCore.buildRequestedAuthnContext(
        spidLevel);
    assertNotNull(requestedAuthnContext);
    assertEquals(AuthnContextComparisonTypeEnumeration.MINIMUM,
        requestedAuthnContext.getComparison());
    if (spidLevel == null || spidLevel.isEmpty()) {
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
    if (idpID.isEmpty()) {
      assertThrows(ConstraintViolationException.class, () ->
          samlUtilsExtendedCore.buildDestination(idpID));
    } else if (idpID.equals("notExistingIdpID")) {
      assertTrue(samlUtilsExtendedCore.buildDestination(idpID).isEmpty());
    } else {
      assertTrue(samlUtilsExtendedCore.buildDestination(idpID).isPresent());
    }
  }

  @ParameterizedTest
  @ValueSource(strings = {"https://idserver.servizicie.interno.gov.it/idp/profile/SAML2/POST/SSO",
      "notExistingIdpID"})
  @EmptySource
  @SneakyThrows
  void getEntityDescriptor(String id) {

    if (id.isEmpty()) {
      assertThrows(ConstraintViolationException.class, () ->
          samlUtilsExtendedCore.getEntityDescriptor(id));
    } else if (id.equals("notExistingIdpID")) {
      assertTrue(samlUtilsExtendedCore.getEntityDescriptor(id).isEmpty());
    } else {
      assertTrue(samlUtilsExtendedCore.getEntityDescriptor(id).isPresent());
    }
  }

  @Test
  void getAttributeDTOListFromAssertion() {
    //Optional<List<AttributeDTO>> attributes = samlUtilsExtendedCore.getAttributeDTOListFromAssertion();

  }

  @Test
  void getSAMLResponseFromString() {
  }

  @Test
  void validateSignature() {
  }

}
