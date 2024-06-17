package it.pagopa.oneid;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Path("/")
public class DynamicRegistration {

  @POST
  @Path("/register")
  public Response register() {
    return Response.ok("Register path").build();
  }

}
