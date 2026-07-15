package it.pagopa.oneid.web.controller;

import io.quarkus.logging.Log;
import io.quarkus.runtime.Startup;
import io.smallrye.common.annotation.RunOnVirtualThread;
import it.pagopa.oneid.common.model.IDP;
import it.pagopa.oneid.service.IdpServiceImpl;
import it.pagopa.oneid.service.OIDCServiceImpl;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Optional;

@Path("/")
@Startup
@RunOnVirtualThread
public class OneIDController {

  @Inject
  OIDCServiceImpl oidcServiceImpl;

  @Inject
  IdpServiceImpl idpServiceImpl;

  @GET
  @Path("/.well-known/openid-configuration")
  @Produces(MediaType.APPLICATION_JSON)
  public Response openIDConfig() {
    Log.debug("start");
    return Response.ok(oidcServiceImpl.buildOIDCProviderMetadata().toJSONObject()).build();
  }

  @GET
  @Path("/idps")
  @Produces(MediaType.APPLICATION_JSON)
  public Response findAllIdp() {
    Log.debug("start");
    Optional<ArrayList<IDP>> idps = idpServiceImpl.findAllIdpByTimestamp();
    Response.ResponseBuilder responseBuilder = idps.isEmpty()
        ? Response.status(404)
        : Response.ok(idps);

    return responseBuilder
        .header("Access-Control-Allow-Origin", "*")
        .build();
  }

}
