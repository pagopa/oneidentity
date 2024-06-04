package it.pagopa.oneid.web.controller;

import it.pagopa.oneid.web.dto.AuthorizationRequestDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestQuery;

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
    public Response login(@NotBlank @RestQuery String idp, @BeanParam @Valid AuthorizationRequestDTO authorizationRequestDTO) {
        return Response.ok("Login Path: " + idp + " ").build();
    }
}
