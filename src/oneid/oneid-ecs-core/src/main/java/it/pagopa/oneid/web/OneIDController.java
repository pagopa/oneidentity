package it.pagopa.oneid.web;


import it.pagopa.oneid.common.DummyClient;
import it.pagopa.oneid.common.DummyUser;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Path("/")
public class OneIDController {

    @GET
    @Path("/ping")
    public Response ping() {
        return Response.ok("Ping").build();
    }

    @GET
    @Path("/login")
    public Response login() {
        return Response.ok(DummyUser.USER_NAME).build();
    }

    @GET
    @Path("/oidc/authorize")
    public Response authorize() {
        return Response.ok(DummyClient.CLIENT_NAME).build();
    }

    @GET
    @Path("/oidc/token")
    public Response token() {
        return Response.ok("Token Path").build();
    }

    @GET
    @Path("/saml/acs")
    public Response samlACS() {
        return Response.ok("ACS Path").build();
    }
}
