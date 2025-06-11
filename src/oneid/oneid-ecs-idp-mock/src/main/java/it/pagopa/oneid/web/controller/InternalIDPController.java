package it.pagopa.oneid.web.controller;

import io.quarkus.runtime.Startup;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Path(("/internal-idp"))
@Startup
public class InternalIDPController {

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
  public Response consent() {
    // This method is a placeholder for the consent endpoint.
    // In a real implementation, you would handle the consent logic here.
    return Response.ok("Consent endpoint hit").build();
  }


}
