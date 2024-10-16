package it.pagopa.oneid.web.controller;

import io.quarkus.logging.Log;
import it.pagopa.oneid.model.dto.ClientRegistrationRequestDTO;
import it.pagopa.oneid.model.dto.ClientRegistrationResponseDTO;
import it.pagopa.oneid.service.ClientRegistrationServiceImpl;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@Path("/oidc")
public class ClientRegistrationController {

  @Inject
  ClientRegistrationServiceImpl clientRegistrationService;

  @POST
  @Path("/register")
  @Produces(MediaType.APPLICATION_JSON)
  public Response register(
      @BeanParam @Valid ClientRegistrationRequestDTO clientRegistrationRequestDTO) {
    Log.info("start");

    clientRegistrationService.validateClientRegistrationInfo(clientRegistrationRequestDTO);

    Log.info("client info validated successfully");

    ClientRegistrationResponseDTO clientRegistrationResponseDTO = clientRegistrationService.saveClient(
        clientRegistrationRequestDTO);

    Log.info("end");

    return Response.ok(clientRegistrationResponseDTO).status(Status.CREATED).build();

  }

  @GET
  @Path("/register/{client_id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response register(@PathParam("client_id") String clientId) {
    return Response.ok(clientRegistrationService.getClientMetadataDTO(
        clientId)).build();
  }

}
