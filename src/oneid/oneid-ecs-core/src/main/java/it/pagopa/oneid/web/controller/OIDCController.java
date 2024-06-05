package it.pagopa.oneid.web.controller;

import it.pagopa.oneid.common.DummyClient;
import it.pagopa.oneid.model.dto.AuthorizationRequestDTO;
import it.pagopa.oneid.web.dto.TokenRequestDTOExtended;
import jakarta.validation.Valid;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Path(("/oidc"))
public class OIDCController {

    @GET
    @Path("/.well-known/openid-configuration")
    public Response openIDConfig() {
        return Response.ok("OIDC Configuration Path").build();
    }

    @GET
    @Path("/keys")
    public Response keys() {
        return Response.ok("OIDC Keys Path").build();
    }

    @POST
    @Path("/authorize")
    public Response authorize(@BeanParam @Valid AuthorizationRequestDTO authorizationRequestDTO) {
        return Response.ok(DummyClient.CLIENT_NAME).build();
    }

    @POST
    @Path("/token")
    public Response token(@BeanParam @Valid TokenRequestDTOExtended tokenRequestDTO) {
        return Response.ok("Token Path").build();
    }

}
