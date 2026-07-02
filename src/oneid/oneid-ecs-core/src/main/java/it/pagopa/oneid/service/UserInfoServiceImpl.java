package it.pagopa.oneid.service;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import io.quarkus.logging.Log;
import it.pagopa.oneid.common.connector.PDVApiClient;
import it.pagopa.oneid.common.model.Client;
import it.pagopa.oneid.common.model.dto.PDVUserUpsertResponseDTO;
import it.pagopa.oneid.common.model.dto.SavePDVUserDTO;
import it.pagopa.oneid.common.utils.SSMConnectorUtilsImpl;
import it.pagopa.oneid.connector.CloudWatchConnectorImpl;
import it.pagopa.oneid.exception.InvalidAccessTokenException;
import it.pagopa.oneid.exception.SessionException;
import it.pagopa.oneid.model.session.AccessTokenSession;
import it.pagopa.oneid.model.session.SAMLSession;
import it.pagopa.oneid.model.session.enums.RecordType;
import it.pagopa.oneid.web.dto.UserInfoResponseDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import java.text.ParseException;
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

  // flag to enable/disable pairwise globally
  @ConfigProperty(name = "pairwise_enabled")
  boolean pairwiseEnabled;

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

  @Override
  public UserInfoResponseDTO getUserInfo(String accessToken) {
    AccessTokenSession accessTokenSession = getValidatedAccessTokenSession(accessToken);
    String clientId = resolveClientId(accessTokenSession);
    UserInfoResponseDTO userInfoResponseDTO = buildUserInfoResponse(accessTokenSession.getIdToken());

    if (userInfoResponseDTO.hasPairwiseClaim()) {
      cloudWatchConnectorImpl.sendUserInfoSuccessMetricData(clientId);
      return userInfoResponseDTO;
    }

    if (StringUtils.isNotBlank(accessTokenSession.getPairwise())) {
      userInfoResponseDTO.setPairwiseClaim(accessTokenSession.getPairwise());
      cloudWatchConnectorImpl.sendUserInfoSuccessMetricData(clientId);
      return userInfoResponseDTO;
    }

    Client client = clientsMap.get(clientId);
    if (!pairwiseEnabled || client == null || !client.isPairwise()) {
      cloudWatchConnectorImpl.sendUserInfoSuccessWithoutPairwiseMetricData(clientId);
      return userInfoResponseDTO;
    }

    Optional<String> pairwiseToken = fetchPairwiseTokenFromPDV(clientId, userInfoResponseDTO);
    pairwiseToken.ifPresentOrElse(pairwise -> {
      userInfoResponseDTO.setPairwiseClaim(pairwise);
      persistPairwiseOnAccessTokenSession(accessToken, pairwise);
      cloudWatchConnectorImpl.sendUserInfoSuccessMetricData(clientId);
    }, () -> cloudWatchConnectorImpl.sendUserInfoSuccessWithoutPairwiseMetricData(clientId));

    return userInfoResponseDTO;
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

  private UserInfoResponseDTO buildUserInfoResponse(String idToken) {
    try {
      JWTClaimsSet claimsSet = SignedJWT.parse(idToken).getJWTClaimsSet();
      UserInfoResponseDTO userInfoResponseDTO = new UserInfoResponseDTO();
      claimsSet.getClaims().forEach(userInfoResponseDTO::addClaim);
      return userInfoResponseDTO;
    } catch (ParseException | RuntimeException e) {
        cloudWatchConnectorImpl.sendUserInfoErrorMetricData(UNKNOWN_CLIENT_ID);
      Log.error("unable to parse id token claims for userinfo");
      throw new InvalidAccessTokenException();
    }
  }

  private String resolveClientId(AccessTokenSession accessTokenSession) {
    try {
      SAMLSession samlSession = samlSessionService.getSession(
          accessTokenSession.getSamlRequestID(),
          RecordType.SAML
      );
      return samlSession.getAuthorizationRequestDTOExtended().getClientId();
    } catch (SessionException e) {
        cloudWatchConnectorImpl.sendUserInfoErrorMetricData(UNKNOWN_CLIENT_ID);
      Log.warn("saml session linked to access token not found");
      throw new InvalidAccessTokenException();
    }
  }

  private Optional<String> fetchPairwiseTokenFromPDV(String clientId,
      UserInfoResponseDTO userInfoResponseDTO) {
    String fiscalNumber = extractFiscalNumber(userInfoResponseDTO);
    if (StringUtils.isBlank(fiscalNumber)) {
      Log.warn("fiscalNumber claim not present in userinfo payload, skipping pairwise loading");
      return Optional.empty();
    }

    Optional<String> apiKey = ssmConnectorUtilsImpl.getParameter(PDV_API_KEY_PREFIX + clientId);
    if (apiKey.isEmpty()) {
      Log.warn("pdv api key not found in parameter store for clientId: " + clientId);
      return Optional.empty();
    }

    SavePDVUserDTO payload = new SavePDVUserDTO(fiscalNumber);

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

  private String extractFiscalNumber(UserInfoResponseDTO userInfoResponseDTO) {
    Object fiscalNumber = userInfoResponseDTO.getClaims().get(FISCAL_NUMBER_CLAIM);
    if (fiscalNumber instanceof String fiscalNumberValue) {
      return fiscalNumberValue;
    }
    return null;
  }

  private void persistPairwiseOnAccessTokenSession(String accessToken, String pairwise) {
    try {
      accessTokenSessionService.setAccessTokenPairwise(accessToken, pairwise);
    } catch (SessionException e) {
      Log.warn("unable to persist pairwise on access token session, proceeding with response");
    }
  }
}
