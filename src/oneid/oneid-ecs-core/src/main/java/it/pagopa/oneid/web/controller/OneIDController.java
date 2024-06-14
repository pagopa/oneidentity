package it.pagopa.oneid.web.controller;

import org.jboss.resteasy.reactive.RestResponse;

import it.pagopa.oneid.model.dto.AuthorizationRequestDTO;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;

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
  @Produces(MediaType.TEXT_HTML)
  public Response login(@BeanParam @Valid AuthorizationRequestDTO authorizationRequestDTO, @Context UriInfo uriInfo) {
    // TODO remove this Mock and set @Valid

    // redirect to landing page with params
    UriBuilder u = uriInfo.getRequestUriBuilder();
    u.replacePath("/");
    return RestResponse.seeOther(u.build()).toResponse();

  }
}
