package it.pagopa.oneid.web.controller;

import io.quarkus.logging.Log;
import it.pagopa.oneid.common.model.ClientFE;
import it.pagopa.oneid.common.model.IDP;
import it.pagopa.oneid.service.ClientServiceImpl;
import it.pagopa.oneid.service.IdpServiceImpl;
import it.pagopa.oneid.service.OIDCServiceImpl;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Optional;

@Path("/")
public class OneIDController {

  @Inject
  OIDCServiceImpl oidcServiceImpl;

  @Inject
  ClientServiceImpl clientServiceImpl;

  @Inject
  IdpServiceImpl idpServiceImpl;

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
    Log.info("start");
    return Response.ok(oidcServiceImpl.buildOIDCProviderMetadata().toJSONObject()).build();
  }

  @GET
  @Path("/client/{client_id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response findClientById(@PathParam("client_id") String clientID) {
    Log.info("start");
    Optional<ClientFE> clientFE = clientServiceImpl.getClientInformation(clientID);
    return clientFE.isEmpty() ?
        Response.status(404).build() :
        Response.ok(clientFE).build();
  }

  @GET
  @Path("/idps")
  @Produces(MediaType.APPLICATION_JSON)
  public Response findAllIdp() {
    Log.info("start");
    Optional<ArrayList<IDP>> idps = idpServiceImpl.findAllIdpByTimestamp();
    return idps.isEmpty() ?
        Response.status(404).build() :
        Response.ok(idps).build();
  }

}
