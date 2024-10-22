package it.pagopa.oneid.web.controller;


import it.pagopa.oneid.common.model.exception.OneIdentityException;
import it.pagopa.oneid.service.IdpMockServiceImpl;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Base64;
import org.jboss.resteasy.reactive.RestForm;
import org.opensaml.saml.saml2.core.AuthnRequest;

@Path("/idp-mock")
public class IdpMockController {

  @Inject
  IdpMockServiceImpl idpMockServiceImpl;


  @POST
  @Path("/sso")
  @Produces(MediaType.TEXT_HTML)
  public Response idpMock(@RestForm String authnRequest, @RestForm String relayState)
      throws OneIdentityException {

    AuthnRequest inputAuthnRequest = idpMockServiceImpl.getAuthnRequestFromString(authnRequest);

    org.opensaml.saml.saml2.core.Response response = idpMockServiceImpl.createSamlResponse(
        inputAuthnRequest);

    String xmlResponse = idpMockServiceImpl.getStringValue(
        idpMockServiceImpl.getElementValueFromSamlResponse(response));

    String xmlResponseBase64 = Base64.getEncoder().encodeToString(xmlResponse.getBytes());

    String redirectAutoSubmitPOSTForm =
        "<form method='post' action=" + inputAuthnRequest.getAssertionConsumerServiceURL()
            + " id='SAMLResponseForm'>" +
            "<input type='hidden' name='SAMLResponse' value=" + xmlResponseBase64 + " />" +
            "<input type='hidden' name='RelayState' value=" + relayState + " />" +
            "<input id='SAMLSubmitButton' type='submit' value='Submit' />" +
            "</form>" +
            "<script>document.getElementById('SAMLSubmitButton').style.visibility='hidden'; " +
            "document.getElementById('SAMLResponseForm').submit();</script>";

    return Response
        .ok(redirectAutoSubmitPOSTForm)
        .type(MediaType.TEXT_HTML)
        .build();

  }

}