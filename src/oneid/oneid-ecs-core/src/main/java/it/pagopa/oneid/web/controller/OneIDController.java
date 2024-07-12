package it.pagopa.oneid.web.controller;

import io.quarkus.logging.Log;
import it.pagopa.oneid.service.OIDCServiceImpl;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/")
public class OneIDController {

  @Inject
  OIDCServiceImpl oidcServiceImpl;

  // TODO replace with quarkus default health check
  @GET
  @Path("/ping")
  public Response ping() {
    return Response.ok("Ping").build();
  }

  @GET
  @Path("/.well-known/openid-configuration")
  @Produces(MediaType.APPLICATION_JSON)
  public Response openIDConfig() {
    Log.info("[OneIDController.openIDConfig] start");
    return Response.ok(oidcServiceImpl.buildOIDCProviderMetadata().toJSONObject()).build();
  }

}
