package it.pagopa.oneid.service;

import it.pagopa.oneid.common.model.IDP;
import it.pagopa.oneid.common.model.enums.AuthLevel;
import it.pagopa.oneid.common.model.enums.SamlBinding;
import it.pagopa.oneid.common.model.exception.OneIdentityException;
import it.pagopa.oneid.common.model.dto.AttributeDTO;
import it.pagopa.oneid.model.session.enums.AuthnContextComparisonType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Response;

public interface SAMLService {

  AuthnRequest buildAuthnRequest(String idpSSOEndpoint, int assertionConsumerServiceIndex,
      int attributeConsumingServiceIndex, String authLevel,
      AuthnContextComparisonType comparisonType, SamlBinding samlBindingType,
      String assertionRef)
      throws OneIdentityException;

  void validateSAMLResponse(Response SAMLResponse, String entityID, Set<String> requestedAttributes,
      Instant samlRequestIssueInstant, AuthLevel authLevelRequest,
      AuthnContextComparisonType comparisonType, String redirectUri, String state,
      String clientId, Integer eidasIndex)
      throws OneIdentityException;

  Response getSAMLResponseFromString(String SAMLResponse) throws OneIdentityException;

  List<AttributeDTO> getAttributesFromSAMLAssertion(Assertion assertion)
      throws OneIdentityException;

  Optional<IDP> getIDPFromEntityID(String entityID);

  void checkSAMLStatus(Response response, String redirectUri, String clientId, String idp,
      String state)
      throws OneIdentityException;

}
