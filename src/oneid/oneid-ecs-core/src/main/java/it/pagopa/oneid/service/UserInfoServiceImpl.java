package it.pagopa.oneid.service;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import io.quarkus.logging.Log;
import it.pagopa.oneid.common.connector.PDVApiClient;
import it.pagopa.oneid.common.model.Client;
import it.pagopa.oneid.common.model.dto.AttributeDTO;
import it.pagopa.oneid.common.model.dto.PDVUserUpsertResponseDTO;
import it.pagopa.oneid.common.model.dto.SavePDVUserDTO;
import it.pagopa.oneid.common.utils.SSMConnectorUtilsImpl;
import it.pagopa.oneid.connector.CloudWatchConnectorImpl;
import it.pagopa.oneid.exception.InvalidAccessTokenException;
import it.pagopa.oneid.exception.OIDCSignJWTException;
import it.pagopa.oneid.exception.SessionException;
import it.pagopa.oneid.model.session.AccessTokenSession;
import it.pagopa.oneid.model.session.SAMLSession;
import it.pagopa.oneid.model.session.enums.RecordType;
import it.pagopa.oneid.service.utils.OIDCUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class UserInfoServiceImpl implements UserInfoService {

  private static final String PDV_API_KEY_PREFIX = "/pdv/";
  private static final String FISCAL_NUMBER_CLAIM = "fiscalNumber";
  private static final String UNKNOWN_CLIENT_ID = "UNKNOWN";
  private static final String PAIRWISE_CLAIM = "pairwise";

  // flag to enable/disable pairwise globally
  @ConfigProperty(name = "pairwise_enabled")
  boolean pairwiseEnabled;

  @ConfigProperty(name = "registry_enabled")
  boolean registryEnabled;

  @Inject
  SessionServiceImpl<AccessTokenSession> accessTokenSessionService;

  @Inject
  SessionServiceImpl<SAMLSession> samlSessionService;

  @Inject
  SSMConnectorUtilsImpl ssmConnectorUtilsImpl;

  @Inject
  @RestClient
  PDVApiClient pdvApiClient;

  @Inject
  Map<String, Client> clientsMap;

  @Inject
  CloudWatchConnectorImpl cloudWatchConnectorImpl;

  @Inject
  OIDCUtils oidcUtils;

  @Override
  public String getSignedUserInfo(String accessToken) {
    AccessTokenSession accessTokenSession = getValidatedAccessTokenSession(accessToken);
    SAMLSession samlSession;

    // retrieve the SAML session linked to the access token
    try {
      samlSession = samlSessionService.getSession(
          accessTokenSession.getSamlRequestID(),
          RecordType.SAML
      );
    } catch (SessionException e) {
      cloudWatchConnectorImpl.sendUserInfoErrorMetricData(UNKNOWN_CLIENT_ID);
      Log.warn("saml session linked to access token not found");
      throw new InvalidAccessTokenException();
    }

    String clientId = samlSession.getAuthorizationRequestDTOExtended().getClientId();

    // retrieve the claims set from the ID token stored in the session
    JWTClaimsSet claimsSet;
    try {
      claimsSet = SignedJWT.parse(accessTokenSession.getIdToken()).getJWTClaimsSet();
    } catch (ParseException | RuntimeException e) {
      cloudWatchConnectorImpl.sendUserInfoErrorMetricData(UNKNOWN_CLIENT_ID);
      Log.error("unable to parse id token claims for userinfo");
      throw new InvalidAccessTokenException();
    }

    String pairwise = claimsSet.getClaim(PAIRWISE_CLAIM) instanceof String pairwiseClaim
        ? pairwiseClaim : null;

    // check if pairwise is in the access token session
    if (StringUtils.isBlank(pairwise) && StringUtils.isNotBlank(accessTokenSession.getPairwise())) {
      pairwise = accessTokenSession.getPairwise();
    }

    // fetch pairwise from PDV if needed
    if (StringUtils.isBlank(pairwise)) {
      Client client = clientsMap.get(clientId);
      if (pairwiseEnabled && client != null && client.isPairwise()) {
        pairwise = fetchPairwiseTokenFromPDV(clientId, claimsSet).orElse(null);
        if (StringUtils.isNotBlank(pairwise)) {
          persistPairwiseOnAccessTokenSession(accessToken, pairwise);
        }
      }
    }

    if (StringUtils.isNotBlank(pairwise)) {
      cloudWatchConnectorImpl.sendUserInfoSuccessMetricData(clientId);
    } else {
      cloudWatchConnectorImpl.sendUserInfoSuccessWithoutPairwiseMetricData(clientId);
    }

    // create a new claims set with the pairwise claim if available
    JWTClaimsSet signedClaimsSet = StringUtils.isNotBlank(pairwise)
        ? new JWTClaimsSet.Builder(claimsSet)
        .claim(PAIRWISE_CLAIM, pairwise)
        .build()
        : claimsSet;

    try {
      return oidcUtils.createSignedJWT(signedClaimsSet);
    } catch (RuntimeException e) {
      throw new OIDCSignJWTException(e);
    }
  }

  private AccessTokenSession getValidatedAccessTokenSession(String accessToken) {
    try {
      return accessTokenSessionService.getSession(accessToken, RecordType.ACCESS_TOKEN);
    } catch (SessionException e) {
      cloudWatchConnectorImpl.sendUserInfoErrorMetricData(UNKNOWN_CLIENT_ID);
      Log.warn("access token not found or expired");
      throw new InvalidAccessTokenException();
    }
  }

  private Optional<String> fetchPairwiseTokenFromPDV(String clientId,
      JWTClaimsSet claimsSet) {
    String fiscalNumber = claimsSet.getClaim(FISCAL_NUMBER_CLAIM) instanceof String fiscalCode
        ? fiscalCode : null;
    if (StringUtils.isBlank(fiscalNumber)) {
      Log.warn("fiscalNumber not present in attribute list, can't generate pairwise sub");
      return Optional.empty();
    }

    // build the list of attributes to send to PDV
    List<AttributeDTO> attributes = new ArrayList<>();
    claimsSet.getClaims().forEach((claimName, claimValue) -> {
      if (claimValue == null) {
        return;
      }
      attributes.add(AttributeDTO.builder()
          .attributeName(claimName)
          .attributeValue(String.valueOf(claimValue))
          .build());
    });

    Optional<String> apiKey = ssmConnectorUtilsImpl.getParameter(PDV_API_KEY_PREFIX + clientId);
    if (apiKey.isEmpty()) {
      Log.warn("pdv api key not found in parameter store for clientId: " + clientId);
      return Optional.empty();
    }

    // send the attributes list only if registry is enabled, otherwise send only fiscal number
    SavePDVUserDTO payload = registryEnabled
        ? SavePDVUserDTO.fromAttributeDtoList(attributes)
        : new SavePDVUserDTO(fiscalNumber);

    try {
      PDVUserUpsertResponseDTO response = pdvApiClient.upsertUser(
          payload,
          apiKey.get()
      );

      if (response == null || StringUtils.isBlank(response.getUserId())) {
        Log.warn("pdv /users response does not contain id");
        return Optional.empty();
      }

      return Optional.of(response.getUserId());
    } catch (WebApplicationException | ProcessingException e) {
      Log.warn("pdv /users call failed, proceeding without pairwise claim");
      return Optional.empty();
    }
  }

  private void persistPairwiseOnAccessTokenSession(String accessToken, String pairwise) {
    try {
      accessTokenSessionService.setAccessTokenPairwise(accessToken, pairwise);
    } catch (SessionException e) {
      Log.warn("unable to persist pairwise on access token session, proceeding with response");
    }
  }
}
