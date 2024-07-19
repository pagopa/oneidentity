package it.pagopa.oneid.web.controller;

import io.quarkus.logging.Log;
import it.pagopa.oneid.model.dto.ClientRegistrationRequestDTO;
import it.pagopa.oneid.model.dto.ClientRegistrationResponseDTO;
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
    Log.info("start");

    clientRegistrationService.validateClientRegistrationInfo(clientRegistrationRequestDTO);

    Log.info("client info validated successfully");

    ClientRegistrationResponseDTO clientRegistrationResponseDTO = clientRegistrationService.saveClient(
        clientRegistrationRequestDTO);

    Log.info("end");

    return Response.ok(clientRegistrationResponseDTO).build();

  }

}
