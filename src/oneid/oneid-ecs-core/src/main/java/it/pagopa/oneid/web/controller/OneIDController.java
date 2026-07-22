package it.pagopa.oneid.web.controller;

import io.quarkus.logging.Log;
import io.quarkus.runtime.Startup;
import io.smallrye.common.annotation.RunOnVirtualThread;
import it.pagopa.oneid.service.OIDCServiceImpl;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/")
@Startup
@RunOnVirtualThread
public class OneIDController {

  @Inject
  OIDCServiceImpl oidcServiceImpl;

  @GET
  @Path("/.well-known/openid-configuration")
  @Produces(MediaType.APPLICATION_JSON)
  public Response openIDConfig() {
    Log.debug("start");
    return Response.ok(oidcServiceImpl.buildOIDCProviderMetadata().toJSONObject()).build();
  }

}
