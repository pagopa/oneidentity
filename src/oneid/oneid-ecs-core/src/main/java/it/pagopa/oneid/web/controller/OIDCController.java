package it.pagopa.oneid.web.controller;

import it.pagopa.oneid.common.DummyClient;
import it.pagopa.oneid.web.dto.AuthorizationRequestDTO;
import it.pagopa.oneid.web.dto.TokenRequestDTO;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
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

    @GET
    @Path("/token")
    public Response token(@HeaderParam("Authorization") String authorization,
                          @BeanParam @Valid TokenRequestDTO tokenRequestDTO) {
        return Response.ok("Token Path").build();
    }

}
