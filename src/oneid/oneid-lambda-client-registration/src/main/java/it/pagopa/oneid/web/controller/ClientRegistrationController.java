package it.pagopa.oneid.web.controller;

import it.pagopa.oneid.model.dto.ClientRegistrationRequestDTO;
import it.pagopa.oneid.service.ClientRegistrationServiceImpl;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/oidc")
public class ClientRegistrationController {

  @Inject
  ClientRegistrationServiceImpl clientRegistrationService;

  @POST
  @Path("/register")
  @Produces(MediaType.APPLICATION_JSON)
  public Response register(ClientRegistrationRequestDTO clientRegistrationRequestDTO) {
    //TODO implement
    return Response.ok("Register path").build();
  }

}
