package it.pagopa.oneid.web.controller;

import it.pagopa.oneid.common.DummyClient;
import it.pagopa.oneid.model.dto.AuthorizationRequestDTO;
import it.pagopa.oneid.service.SAMLService;
import it.pagopa.oneid.service.SAMLServiceImpl;
import it.pagopa.oneid.service.utils.SAMLUtils;
import it.pagopa.oneid.web.dto.TokenRequestDTOExtended;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.security.SecurityException;
import org.opensaml.xmlsec.signature.support.SignatureException;

@Path(("/oidc"))
public class OIDCController {
    @Inject
    private SAMLServiceImpl samlServiceImpl;

    @GET
    @Path("/.well-known/openid-configuration")
    @Produces(MediaType.APPLICATION_JSON)
    public Response openIDConfig() {
        return Response.ok("OIDC Configuration Path").build();
    }

    @GET
    @Path("/keys")
    @Produces(MediaType.APPLICATION_JSON)
    public Response keys() {
        return Response.ok("OIDC Keys Path").build();
    }

    @POST
    @Path("/authorize")
    public Response authorize(@BeanParam @Valid AuthorizationRequestDTO authorizationRequestDTO) throws SecurityException, SignatureException, MarshallingException {
        //AuthnRequest authnRequest = samlServiceImpl.buildAuthnRequest("https://id.eht.eu", 0,0,"","");
        return Response.ok(DummyClient.CLIENT_NAME).build();
    }

    @POST
    @Path("/token")
    @Produces(MediaType.APPLICATION_JSON)
    public Response token(@BeanParam @Valid TokenRequestDTOExtended tokenRequestDTOExtended) {
        return Response.ok("Token Path").build();
    }

}
