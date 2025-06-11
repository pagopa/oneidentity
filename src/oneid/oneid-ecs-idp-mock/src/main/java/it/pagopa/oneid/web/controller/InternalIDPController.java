package it.pagopa.oneid.web.controller;

import io.quarkus.runtime.Startup;
import it.pagopa.oneid.service.InternalIDPServiceImpl;
import it.pagopa.oneid.service.SessionServiceImpl;
import it.pagopa.oneid.web.dto.ConsentRequestDTO;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Path(("/internal-idp"))
@Startup
public class InternalIDPController {

  @ConfigProperty(name = "acs_endpoint")
  String ACS_ENDPOINT;

  @Inject
  InternalIDPServiceImpl internalIDPServiceImpl;

  @Inject
  SessionServiceImpl sessionServiceImpl;

  @POST
  @Path("/samlsso")
  public Response samlSso() {
    // This method is a placeholder for the SAML SSO endpoint.
    // In a real implementation, you would handle the SAML SSO logic here.
    return Response.ok("SAML SSO endpoint hit").build();
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
  public Response consent(@Valid ConsentRequestDTO consentRequestDto) {

    sessionServiceImpl.validateAuthnRequestIdCookie(consentRequestDto.getAuthnRequestId(),
        consentRequestDto.getUsername());

    return consentRequestDto.isConsent() ? handleConsentGiven(consentRequestDto)
        : handleConsentDenied(consentRequestDto);
  }

  private Response handleConsentGiven(ConsentRequestDTO consentRequestDto) {
    // TODO: implement the logic to handle the case where consent is given. If the consent is given, proceed with the SAML SSO flow creating a SAML Response that will be sent back to the SP (Service Provider) via POST binding using Redirect-POST through the browser.
    return Response.ok(getRedirectAutoSubmitPOSTForm(ACS_ENDPOINT, "")).type(MediaType.TEXT_HTML)
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
