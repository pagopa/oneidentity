package it.pagopa.oneid.web.controller;

import io.quarkus.logging.Log;
import it.pagopa.oneid.common.model.Client;
import it.pagopa.oneid.common.model.exception.OneIdentityException;
import it.pagopa.oneid.common.model.exception.SAMLUtilsException;
import it.pagopa.oneid.model.IDPSession;
import it.pagopa.oneid.model.enums.IDPSessionStatus;
import it.pagopa.oneid.service.InternalIDPServiceImpl;
import it.pagopa.oneid.service.SessionServiceImpl;
import it.pagopa.oneid.web.dto.ConsentRequestDTO;
import it.pagopa.oneid.web.dto.LoginRequestDTO;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.Base64;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.reactive.RestForm;
import org.opensaml.saml.saml2.core.AuthnRequest;

@Path(("/"))
public class InternalIDPController {

  @ConfigProperty(name = "acs_endpoint")
  String ACS_ENDPOINT;

  @ConfigProperty(name = "idp_login_endpoint")
  String IDP_LOGIN_ENDPOINT;


  @ConfigProperty(name = "idp_consent_endpoint")
  String IDP_CONSENT_ENDPOINT;

  @Inject
  InternalIDPServiceImpl internalIDPServiceImpl;

  @Inject
  SessionServiceImpl sessionServiceImpl;


  @POST
  @Path("/samlsso")
  @Produces(MediaType.TEXT_HTML)
  public Response samlSso(@RestForm("SAMLRequest") String authnRequestString) {
    // Parse and validate AuthnRequest
    AuthnRequest authnRequest = internalIDPServiceImpl.getAuthnRequestFromString(
        authnRequestString);
    internalIDPServiceImpl.validateAuthnRequest(authnRequest);

    // Get client by AttributeConsumingServiceIndex from AuthnRequest and save it
    Client client = internalIDPServiceImpl.getClientByAttributeConsumingServiceIndex(
        authnRequest);
    sessionServiceImpl.saveIDPSession(authnRequest, client);

    // Set authnRequestId and clientId inside cookies
    NewCookie authnRequestIdCookie = new NewCookie.Builder("AuthnRequestId")
        .value(authnRequest.getID())
        .maxAge(3600) // 1 hour
        .httpOnly(true)
        .secure(true)
        .build();
    NewCookie clientIdCookie = new NewCookie.Builder("ClientId")
        .value(client.getClientId())
        .maxAge(3600) // 1 hour
        .httpOnly(true)
        .secure(true)
        .build();

    try {
      return Response.status(302).location(new URI(IDP_LOGIN_ENDPOINT))
          .cookie(authnRequestIdCookie, clientIdCookie).build();
    } catch (URISyntaxException e) {
      Log.error(ExceptionUtils.getStackTrace(e));
      throw new RuntimeException(e);
    }
  }

  @POST
  @Path("/login")
  public Response login(@Valid LoginRequestDTO loginRequestDTO) {

    // 1. check authnRequest
    //  a. AuthnRequestId presents in IdPSession Table
    //  b. Status == "PENDING"
    IDPSession idpSession;
    try {
      idpSession = sessionServiceImpl.validateAuthnRequestIdStatus(
          loginRequestDTO.getAuthnRequestId(), loginRequestDTO.getClientId(),
          IDPSessionStatus.PENDING);
    } catch (OneIdentityException e) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity("Invalid AuthnRequestId or status").build();
    }

    // 2. validate login information
    //  a. correct username and password in User Table for the retrieved ClientId
    try {
      internalIDPServiceImpl.validateUserInformation(
          idpSession.getClientId(),
          loginRequestDTO.getUsername(),
          loginRequestDTO.getPassword());
    } catch (OneIdentityException e) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity("Invalid username or password").build();
    }

    // 3. Update IdPSession
    //  a. insert username
    //  b. update status to "CREDENTIAL_VALIDATED"
    idpSession.setStatus(IDPSessionStatus.CREDENTIALS_VALIDATED);
    idpSession.setUsername(loginRequestDTO.getUsername());
    sessionServiceImpl.updateIdPSession(idpSession);

    // 3. Update Cookie with authnRequestId, username and clientId
    NewCookie authnRequestIdCookie = new NewCookie.Builder("AuthnRequestId")
        .value(idpSession.getAuthnRequestId())
        .maxAge(3600) // 1 hour
        .httpOnly(true)
        .secure(true)
        .build();
    NewCookie usernameCookie = new NewCookie.Builder("username")
        .value(idpSession.getUsername())
        .maxAge(3600) // 1 hour
        .httpOnly(true)
        .secure(true)
        .build();
    NewCookie clientIdCookie = new NewCookie.Builder("ClientId")
        .value(idpSession.getClientId())
        .maxAge(3600) // 1 hour
        .httpOnly(true)
        .secure(false)
        .build();

    try {
      return Response.status(302).location(new URI(IDP_CONSENT_ENDPOINT))
          .cookie(authnRequestIdCookie, usernameCookie, clientIdCookie).build();
    } catch (URISyntaxException e) {
      Log.error(ExceptionUtils.getStackTrace(e));
      throw new RuntimeException(e);
    }

  }

  @POST
  @Path("/consent")
  public Response consent(@Valid ConsentRequestDTO consentRequestDto) throws SAMLUtilsException {

    IDPSession idpSession = sessionServiceImpl.validateAuthnRequestIdCookie(
        consentRequestDto.getAuthnRequestId(),
        consentRequestDto.getClientId(),
        consentRequestDto.getUsername());

    return consentRequestDto.isConsent() ? handleConsentGiven(idpSession)
        : handleConsentDenied(consentRequestDto);
  }

  private Response handleConsentGiven(IDPSession idpSession) throws SAMLUtilsException {

    // Create a successful SAML Response based on the AuthnRequest

    org.opensaml.saml.saml2.core.Response response = internalIDPServiceImpl
        .createSuccessfulSamlResponse(idpSession.getAuthnRequestId(), idpSession.getClientId(),
            idpSession.getUsername());

    String encodedSamlResponse = Base64.getEncoder()
        .encodeToString(internalIDPServiceImpl.getStringValue(
            internalIDPServiceImpl.getElementValueFromSamlResponse(response)).getBytes());

    idpSession.setStatus(IDPSessionStatus.AUTHENTICATED);
    idpSession.setTimestampEnd(Instant.now().getEpochSecond());
    sessionServiceImpl.setSessionAsAuthenticated(idpSession);

    return Response.ok(getRedirectAutoSubmitPOSTForm(ACS_ENDPOINT, encodedSamlResponse))
        .type(MediaType.TEXT_HTML)
        .build();
  }

  private Response handleConsentDenied(ConsentRequestDTO consentRequestDto) {
    // TODO: implement the logic to handle the case where consent is denied. If the consent is not given, proceed with the appropriate SAML Response with the corresponding state value to communicate the denying that will be sent back to the SP (Service Provider) via POST binding using Redirect-POST through the browser.
    return Response.ok(getRedirectAutoSubmitPOSTForm(ACS_ENDPOINT, "")).type(MediaType.TEXT_HTML)
        .build();
  }

  private String getRedirectAutoSubmitPOSTForm(String url, String samlResponse) {
    return "<form id='samlResponseForm' action='" + url + "' method='POST'>"
        + "<input type='hidden' name='SAMLResponse' value='" + samlResponse + "'>"
        + "</form>"
        + "<script>document.getElementById('samlResponseForm').submit();</script>";
  }


}
