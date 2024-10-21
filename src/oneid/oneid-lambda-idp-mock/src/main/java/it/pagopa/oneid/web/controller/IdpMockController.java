package it.pagopa.oneid.web.controller;


import it.pagopa.oneid.common.model.exception.OneIdentityException;
import it.pagopa.oneid.service.IdpMockServiceImpl;
import it.pagopa.oneid.web.utils.WebUtils;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestForm;
import org.opensaml.saml.saml2.core.AuthnRequest;

@Path("/idp-mock")
public class IdpMockController {

  @Inject
  IdpMockServiceImpl idpMockServiceImpl;


  @POST
  @Path("/sso")
  @Produces(MediaType.APPLICATION_XML)
  public Response idpMock(@RestForm String authnRequest, @RestForm String relayState)
      throws OneIdentityException {

    AuthnRequest inputAuthnRequest = idpMockServiceImpl.getAuthnRequestFromString(authnRequest);

    org.opensaml.saml.saml2.core.Response response = idpMockServiceImpl.createSamlResponse(
        inputAuthnRequest);

    String xmlResponse = WebUtils.getStringValue(
        WebUtils.getElementValueFromSamlResponse(response));

    return Response.ok(xmlResponse).type(MediaType.APPLICATION_XML).build();
  }

}
