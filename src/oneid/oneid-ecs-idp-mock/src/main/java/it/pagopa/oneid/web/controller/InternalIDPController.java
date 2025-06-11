package it.pagopa.oneid.web.controller;

import io.quarkus.runtime.Startup;
import it.pagopa.oneid.common.model.exception.OneIdentityException;
import it.pagopa.oneid.service.InternalIDPServiceImpl;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestForm;
import org.opensaml.saml.saml2.core.AuthnRequest;

@Path(("/internal-idp"))
@Startup
public class InternalIDPController {

  @Inject
  InternalIDPServiceImpl internalIDPServiceImpl;

  @POST
  @Path("/samlsso")
  @Produces(MediaType.TEXT_HTML)
  public Response samlSso(@RestForm("SAMLRequest") String authnRequest)
      throws OneIdentityException {

    //from input string to AuthnRequest object
    AuthnRequest inputAuthnRequest = internalIDPServiceImpl.getAuthnRequestFromString(authnRequest);

    //todo validate AuthnRequest

    //todo save on idpSessions table

    //todo return response with set cookie

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
  public Response consent(ConsentRequestDTO consentRequest) {

    // This endpoint will receive consent requests.
    // In particular, it will handle the consent for the internal IDP after the user has logged in.
    // The user will be redirected to this endpoint after logging in, and will be asked to give consent or not to the internal IDP.
    // Inside the body we find the consent boolean value, which is true if the user has given consent and false if the user has not given consent.

    // 1. Extract the consent value from the request body.
    // 2. Extract the AuthnRequestID cookie value previously set by the SAML SSO endpoint
    // 3. Validate the AuthnRequestID cookie value against the session store to ensure it is valid and not expired.
    // 4. If the consent is given, proceed with the SAML SSO flow.
    // 5. If the consent is not given, proceed with the appropriate SAML Response with the corresponding state value to communicate the denying.
    // 6. The response will be a SAML Response that will be sent back to the SP (Service Provider) via POST binding using Redirect-POST through the browser.

    return Response.ok("Consent endpoint hit").build();
  }


}
