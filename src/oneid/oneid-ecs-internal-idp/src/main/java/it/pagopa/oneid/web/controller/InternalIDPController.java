package it.pagopa.oneid.web.controller;

import it.pagopa.oneid.common.model.exception.OneIdentityException;
import it.pagopa.oneid.common.model.exception.SAMLUtilsException;
import it.pagopa.oneid.model.IDPSession;
import it.pagopa.oneid.model.enums.IDPSessionStatus;
import it.pagopa.oneid.service.InternalIDPServiceImpl;
import it.pagopa.oneid.service.SessionServiceImpl;
import it.pagopa.oneid.web.dto.ConsentRequestDTO;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import java.time.Instant;
import java.util.Base64;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.reactive.RestForm;
import org.opensaml.saml.saml2.core.AuthnRequest;

@Path(("/internal-idp"))
public class InternalIDPController {

  @ConfigProperty(name = "acs_endpoint")
  String ACS_ENDPOINT;

  @Inject
  InternalIDPServiceImpl internalIDPServiceImpl;

  @Inject
  SessionServiceImpl sessionServiceImpl;


  @POST
  @Path("/samlsso")
  @Produces(MediaType.TEXT_HTML)
  public Response samlSso(@RestForm("SAMLRequest") String authnRequestString)
      throws OneIdentityException {
    // Parse and validate AuthnRequest
    AuthnRequest authnRequest = internalIDPServiceImpl.getAuthnRequestFromString(
        authnRequestString);
    internalIDPServiceImpl.validateAuthnRequest(authnRequest);
    internalIDPServiceImpl.saveIDPSession(authnRequest);

    // Set authnRequestId as a cookie and return response
    NewCookie cookie = new NewCookie(
        "authnRequestId",
        authnRequest.getID()
    );
    return Response.ok("SAML SSO endpoint hit").cookie(cookie).build();
  }

  @POST
  @Path("/login")
  public Response login() {
    // This method is a placeholder for the login endpoint.
    // In a real implementation, you would handle the login logic here.
    return Response.ok("Login endpoint hit").build();
  }

  @POST
  @Path("/consent")
  public Response consent(@Valid ConsentRequestDTO consentRequestDto) throws SAMLUtilsException {

    IDPSession idpSession = sessionServiceImpl.validateAuthnRequestIdCookie(
        consentRequestDto.getAuthnRequestId(),
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
