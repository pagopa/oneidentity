package it.pagopa.oneid;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Path("/")
public class ServiceMetadata {

  @GET
  @Path("/metadata")
  public Response metadata() {
    return Response.ok("Metadata path").build();
  }
}
