package it.pagopa.oneid.service;

import io.quarkus.logging.Log;
import it.pagopa.oneid.common.model.Client;
import it.pagopa.oneid.common.model.exception.OneIdentityException;
import it.pagopa.oneid.common.utils.logging.CustomLogging;
import it.pagopa.oneid.connector.SessionConnectorImpl;
import it.pagopa.oneid.exception.ClientNotFoundException;
import it.pagopa.oneid.exception.IDPSessionNotFoundException;
import it.pagopa.oneid.exception.InvalidIDPSessionStatusException;
import it.pagopa.oneid.model.IDPSession;
import it.pagopa.oneid.model.enums.IDPSessionStatus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.Instant;
import java.util.Optional;
import org.opensaml.saml.saml2.core.AuthnRequest;

@ApplicationScoped
@CustomLogging
public class SessionServiceImpl implements SessionService {

  @Inject
  SessionConnectorImpl sessionConnectorImpl;

  @Override
  public void saveIDPSession(AuthnRequest authnRequest, Client client) {
    Log.debug("Start saveIDPSession for authnRequestId: " + authnRequest.getID());
    if (client == null) {
      Log.error("Client not found for AttributeConsumingServiceIndex: "
          + authnRequest.getAttributeConsumingServiceIndex());
      throw new ClientNotFoundException("Client not found for AttributeConsumingServiceIndex: "
          + authnRequest.getAttributeConsumingServiceIndex());
    }

    IDPSession idpSession = IDPSession.builder()
        .authnRequestId(authnRequest.getID())
        .clientId(client.getClientId())
        .status(IDPSessionStatus.PENDING)
        .username("")
        .timestampStart(Instant.now().toEpochMilli())
        .timestampEnd(0)
        .build();
    sessionConnectorImpl.saveIDPSessionIfNotExists(idpSession);
    Log.debug("End saveIDPSession for authnRequestId: " + authnRequest.getID());
  }

  @Override
  public IDPSession validateAuthnRequestIdCookie(String authnRequestId, String clientId,
      String username) {

    IDPSession idpSession = sessionConnectorImpl
        .getIDPSessionByAuthnRequestIdClientIdAndUsername(authnRequestId, clientId, username)
        .orElseThrow(() -> new IDPSessionNotFoundException(
            "IDP session not found for authnRequestId: " + authnRequestId + " and username: "
                + username));

    if (!IDPSessionStatus.CREDENTIALS_VALIDATED.equals(idpSession.getStatus())) {
      throw new InvalidIDPSessionStatusException("Invalid IDP session status: "
          + idpSession.getStatus() + ". Expected: " + IDPSessionStatus.CREDENTIALS_VALIDATED);
    }

    return idpSession;
  }

  @Override
  public void setSessionAsAuthenticated(IDPSession idpSession) {
    sessionConnectorImpl.updateIDPSession(idpSession,
        Optional.of(IDPSessionStatus.CREDENTIALS_VALIDATED));
  }

  @Override
  public IDPSession validateAuthnRequestIdStatus(String authnRequestId, String clientId,
      IDPSessionStatus status)
      throws OneIdentityException {
    Optional<IDPSession> optionalIdpSession = sessionConnectorImpl.findIDPSessionByAuthnRequestIdAndClientId(
        authnRequestId, clientId);

    // Check if the session exists
    if (optionalIdpSession.isEmpty()) {
      throw new OneIdentityException(
          "No session with this authnRequestId found: " + authnRequestId); //todo check
    }

    IDPSession idpSession = optionalIdpSession.get();

    // Check if the status matches the session's status
    if (!status.equals(idpSession.getStatus())) {
      throw new OneIdentityException("Invalid status"); //todo check
    }

    return idpSession;
  }

  @Override
  public void updateIdPSession(IDPSession idpSession) {

    // Update the session in the database
    sessionConnectorImpl.updateIDPSession(idpSession, Optional.of(IDPSessionStatus.PENDING));
  }
}
