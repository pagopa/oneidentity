package it.pagopa.oneid.web.controller;

import it.pagopa.oneid.web.dto.AuthorizationRequestDTOExtended;
import jakarta.validation.Valid;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;


@Path("/")
public class OneIDController {

    // TODO replace with quarkus default health check
    @GET
    @Path("/ping")
    public Response ping() {
        return Response.ok("Ping").build();
    }

    @GET
    @Path("/login")
    public Response login(@BeanParam @Valid AuthorizationRequestDTOExtended authorizationRequestDTOExtended) {
        return Response.ok("Login Path: " + authorizationRequestDTOExtended.getIdp() + " ").build();
    }
}
