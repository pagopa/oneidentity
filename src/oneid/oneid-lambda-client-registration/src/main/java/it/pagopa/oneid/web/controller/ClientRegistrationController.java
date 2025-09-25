package it.pagopa.oneid.web.controller;

import static it.pagopa.oneid.service.utils.ClientUtils.getUseridFromBearer;
import io.quarkus.logging.Log;
import it.pagopa.oneid.common.model.Client;
import it.pagopa.oneid.common.model.ClientExtended;
import it.pagopa.oneid.common.model.enums.AuthLevel;
import it.pagopa.oneid.model.dto.ClientRegistrationDTO;
import it.pagopa.oneid.model.dto.ClientRegistrationResponseDTO;
import it.pagopa.oneid.model.enums.EnvironmentMapping;
import it.pagopa.oneid.model.groups.ValidationGroups.Registration;
import it.pagopa.oneid.model.groups.ValidationGroups.UpdateClient;
import it.pagopa.oneid.service.ClientRegistrationServiceImpl;
import it.pagopa.oneid.service.utils.ClientUtils;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.groups.ConvertGroup;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.services.sns.SnsClient;

@Path("/oidc")
public class ClientRegistrationController {

  @Inject
  ClientRegistrationServiceImpl clientRegistrationService;

  @Inject
  SnsClient sns;

  @ConfigProperty(name = "sns_topic_arn")
  String topicArn;

  @ConfigProperty(name = "sns_topic_notification_environment")
  String environment;


  @POST
  @Path("/register")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Response register(
      @Valid @ConvertGroup(to = Registration.class) ClientRegistrationDTO clientRegistrationDTOInput,
      @HeaderParam(HttpHeaders.AUTHORIZATION) String bearer) {
    Log.info("start");

    //1. Extract userId from bearer token
    String userId = getUseridFromBearer(bearer);
    Log.info("userId retrieved from bearer token successfully");

    //2. Validate client infos
    clientRegistrationService.validateClientRegistrationInfo(clientRegistrationDTOInput);
    Log.info("client info validated successfully");

    //3. Save client in db
    ClientRegistrationResponseDTO clientRegistrationResponseDTO = clientRegistrationService.saveClient(
        clientRegistrationDTOInput, userId);
    Log.info(
        "client saved successfully with clientId: " + clientRegistrationResponseDTO.getClientId());

    //4. Prepare message for sns notification
    String message =
        "Name: " + clientRegistrationResponseDTO.getClientName() + "\n" +
            "Client ID: " + clientRegistrationResponseDTO.getClientId() + "\n" +
            "Attributes: " + clientRegistrationResponseDTO.getSamlRequestedAttributes() + "\n" +
            "Redirect URIs: " + clientRegistrationResponseDTO.getRedirectUris();

    String subject =
        "New Client registered in " + EnvironmentMapping.valueOf(environment).getEnvLong();

    try {
      sns.publish(p ->
          p.topicArn(topicArn).subject(subject).message(message));
    } catch (Exception e) {
      Log.log(EnvironmentMapping.valueOf(environment).getLogLevel(),
          "Failed to send SNS notification: ", e);
    }

    Log.info("end");
    return Response.status(Status.CREATED).entity(clientRegistrationResponseDTO).build();

  }

  @GET
  @Path("/register")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Response getClient(@HeaderParam(HttpHeaders.AUTHORIZATION) String bearer) {
    Log.info("start");

    //1. Extract userId from bearer token
    String userId = getUseridFromBearer(bearer);
    Log.info("userId retrieved from bearer token successfully");

    //2. Verify if client exists and if so retrieves it from db
    Client client = clientRegistrationService.getClientByUserId(userId);
    Log.info("client exists for userId: " + userId);

    //3. Convert client to ClientRegistrationDTO
    ClientRegistrationDTO clientRegistrationDTO = ClientUtils.convertClientToClientRegistrationDTO(
        client);

    //4. Set clientId in ClientRegistrationResponseDTO
    ClientRegistrationResponseDTO clientRegistrationResponseDTO = new ClientRegistrationResponseDTO(
        clientRegistrationDTO,
        client.getClientId());

    Log.info("end");
    return Response.ok(clientRegistrationResponseDTO).build();
  }

  @PUT
  @Path("/register/client_id/{client_id}")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Response updateClient(
      @Valid @ConvertGroup(to = UpdateClient.class) ClientRegistrationDTO clientRegistrationDTOInput,
      @PathParam("client_id") String clientId,
      @HeaderParam(HttpHeaders.AUTHORIZATION) String bearer) {
    Log.info("start");

    //1. Extract userId from bearer token
    String userId = getUseridFromBearer(bearer);
    Log.info("userId retrieved from bearer token successfully");

    //2. Validate client infos
    clientRegistrationService.validateClientRegistrationInfo(
        clientRegistrationDTOInput);
    Log.info("client info validated successfully");

    //2. Retrieves client from db
    ClientExtended clientExtended = clientRegistrationService.getClientExtendedByClientId(clientId);

    //3. Check if userId in input matches the client userId on db
    ClientUtils.checkUserId(userId, clientExtended.getUserId());
    Log.info("client exists for clientId: " + clientId);

    //5. Update client infos
    clientRegistrationService.updateClientExtended(clientRegistrationDTOInput,
        clientExtended);
    Log.info("client updated successfully for clientId: " + clientId);

    //6a. Prepare message for sns notification
    String message =
        "Name: " + clientRegistrationDTOInput.getClientName() + "\n" +
            "Client ID: " + clientId + "\n";
    boolean sendNotification = false;

    // Add information if redirectUris or metadata-related fields are updated
    if (!clientRegistrationDTOInput.getRedirectUris().equals(clientExtended.getCallbackURI())) {
      message += "- Redirect URIs updated \n";
      sendNotification = true;
    }
    Optional<String> updatedFields = getUpdateMessage(clientRegistrationDTOInput, clientExtended);
    if (updatedFields.isPresent()) {
      message += "- Metadata related fields updated: [" + updatedFields.get() + "]\n";
      sendNotification = true;
    }

    //7. Send SNS notification only if redirectUris or metadata-related fields has been updated
    if (sendNotification) {
      String subject =
          "Client updated in " + EnvironmentMapping.valueOf(environment).getEnvLong();
      try {

        String finalMessage = message;
        sns.publish(p ->
            p.topicArn(topicArn).subject(subject).message(finalMessage));
      } catch (Exception e) {
        Log.log(EnvironmentMapping.valueOf(environment).getLogLevel(),
            "Failed to send SNS notification: ", e);
      }
    }
    Log.info("end");

    return Response.noContent().build();
  }

  @POST
  @Path("/clients/{client_id}/secret/refresh")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Response refreshClientSecret(
      @PathParam("client_id") String clientId,
      @HeaderParam(HttpHeaders.AUTHORIZATION) String bearer) {
    Log.info("start");

    //1. Extract userId from bearer token
    String userId = getUseridFromBearer(bearer);
    Log.info("userId retrieved from bearer token successfully");

    //2. Refresh client secret
    String secret = clientRegistrationService.refreshClientSecret(
        clientId, userId);
    Map<String, String> response = new HashMap<>();
    response.put("newClientSecret", secret);

    //3. Prepare message for sns notification
    String message =
        "Client ID: " + clientId + "\n";

    String subject =
        "Client Secret refreshed in " + EnvironmentMapping.valueOf(environment).getEnvLong();
    try {
      sns.publish(p ->
          p.topicArn(topicArn).subject(subject).message(message));
    } catch (Exception e) {
      Log.log(EnvironmentMapping.valueOf(environment).getLogLevel(),
          "Failed to send SNS notification: ", e);
    }

    return Response.ok(response).build();
  }

}
