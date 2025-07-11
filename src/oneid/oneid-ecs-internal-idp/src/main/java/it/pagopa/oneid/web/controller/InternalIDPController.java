package it.pagopa.oneid.web.controller;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import it.pagopa.oneid.common.model.Client;
import it.pagopa.oneid.common.model.exception.OneIdentityException;
import it.pagopa.oneid.common.model.exception.SAMLUtilsException;
import it.pagopa.oneid.model.IDPSession;
import it.pagopa.oneid.model.enums.IDPSessionStatus;
import it.pagopa.oneid.service.InternalIDPServiceImpl;
import it.pagopa.oneid.service.SessionServiceImpl;
import it.pagopa.oneid.web.controller.utils.RequestedParameterUtils;
import it.pagopa.oneid.web.dto.ConsentRequestDTO;
import it.pagopa.oneid.web.dto.LoginRequestDTO;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import java.time.Instant;
import java.util.Base64;
import java.util.Set;
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

  @Inject
  Template login; // matches login.html

  @Inject
  Template consent; // matches consent.html

  @Inject
  Template error; // matches error.html

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

    // Set authnRequestId and clientId as hidden form fields instead of cookies
    TemplateInstance instance = login.data("loginAction", IDP_LOGIN_ENDPOINT)
        .data("authnRequestId", authnRequest.getID())
        .data("clientId", client.getClientId());

    return Response.status(Status.OK)
        .entity(instance.render())
        .type(MediaType.TEXT_HTML)
        .build();
  }


  @POST
  @Path("/login")
  @Produces(MediaType.TEXT_HTML)
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
      return Response.status(Status.OK)
          .entity(error.data("errorMessage", "Session error"))
          .type(MediaType.TEXT_HTML)
          .build();
    }

    // 2. validate login information
    //  a. correct username and password in User Table for the retrieved ClientId
    try {
      internalIDPServiceImpl.validateUserInformation(
          idpSession.getClientId(),
          loginRequestDTO.getUsername(),
          loginRequestDTO.getPassword());
    } catch (OneIdentityException e) {
      return Response.status(Status.OK)
          .entity(error.data("errorMessage", "Validation error"))
          .type(MediaType.TEXT_HTML)
          .build();
    }

    // 3. Update IdPSession
    //  a. insert username
    //  b. update status to "CREDENTIAL_VALIDATED"
    idpSession.setStatus(IDPSessionStatus.CREDENTIALS_VALIDATED);
    idpSession.setUsername(loginRequestDTO.getUsername());
    sessionServiceImpl.updateIdPSession(idpSession);

    Set<String> friendlyRequestedParameters = RequestedParameterUtils.mapToFriendlyNames(
        internalIDPServiceImpl.retrieveClientRequestedParameters(idpSession.getClientId()));

    // Pass authnRequestId, username, and clientId as hidden form fields to consent template
    TemplateInstance instance = consent
        .data("consentAction", IDP_CONSENT_ENDPOINT)
        .data("authnRequestId", idpSession.getAuthnRequestId())
        .data("clientId", idpSession.getClientId())
        .data("username", idpSession.getUsername())
        .data("dataList", friendlyRequestedParameters);

    return Response.status(Status.OK)
        .entity(instance.render())
        .type(MediaType.TEXT_HTML)
        .build();

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
        + "<input type='hidden' name='SAMLResponse' value='" + samlResponse + "'/>"
        + "<input type='hidden' name='RelayState' value='internal-idp'/>"
        + "</form>"
        + "<script>document.getElementById('samlResponseForm').submit();</script>";
  }


}
