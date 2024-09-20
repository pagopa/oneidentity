package it.pagopa.oneid.connector;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/idp-keys")
@RegisterRestClient(configKey = "is-api")
public interface ISRestClient {

  @GET
  @Path("/{type}/{timestamp}")
  @Produces(MediaType.APPLICATION_XML)
  String getMetadata(@PathParam("type") String type, @PathParam("timestamp") String timestamp);
}
