package it.pagopa.oneid.web.controller;

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

}
