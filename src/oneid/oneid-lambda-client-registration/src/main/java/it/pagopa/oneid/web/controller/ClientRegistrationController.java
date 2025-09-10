package it.pagopa.oneid.web.controller;

import io.quarkus.logging.Log;
import it.pagopa.oneid.common.model.Client;
import it.pagopa.oneid.model.dto.ClientRegistrationDTO;
import it.pagopa.oneid.model.dto.ClientRegistrationResponseDTO;
import it.pagopa.oneid.model.enums.EnvironmentMapping;
import it.pagopa.oneid.model.groups.ValidationGroups;
import it.pagopa.oneid.service.ClientRegistrationServiceImpl;
import it.pagopa.oneid.service.utils.ClientUtils;
import it.pagopa.oneid.web.dto.RefreshTokenRequestDTO;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.groups.ConvertGroup;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
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

  private static Optional<String> getUpdateMessage(ClientRegistrationDTO input) {
    String message = "";
    if (input.getClientName() != null) {
      message += "ClientName; ";
    }
    if (input.getSamlRequestedAttributes() != null && !input.getSamlRequestedAttributes()
        .isEmpty()) {
      message += "SamlRequestedAttributes; ";
    }
    if (input.getDefaultAcrValues() != null && !input.getDefaultAcrValues().isEmpty()) {
      message += "DefaultAcrValues; ";
    }
    if (StringUtils.isNotBlank(message)) {
      return Optional.of(message);
    }
    return Optional.empty();
  }

  @POST
  @Path("/register")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Response register(
      @Valid @ConvertGroup(to = ValidationGroups.Registration.class) ClientRegistrationDTO clientRegistrationDTOInput) {
    Log.info("start");

    clientRegistrationService.validateClientRegistrationInfo(clientRegistrationDTOInput);

    Log.info("client info validated successfully");

    ClientRegistrationResponseDTO clientRegistrationResponseDTO = clientRegistrationService.saveClient(
        clientRegistrationDTOInput);
    Log.info(
        "client saved successfully with clientId: " + clientRegistrationResponseDTO.getClientId());

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
  @Path("/register/user_id/{user_id}")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Response getClient(@PathParam("user_id") String userId) {
    Log.info("start");

    //1. Verify if client exists and if so retrieves it from db
    Client client = clientRegistrationService.getClientByUserId(userId);
    Log.info("client exists for userId: " + userId);

    //2. Convert client to ClientRegistrationDTO
    ClientRegistrationDTO clientRegistrationDTO = ClientUtils.convertClientToClientRegistrationDTO(
        client);

    //3. Set clientId in ClientRegistrationResponseDTO
    ClientRegistrationResponseDTO clientRegistrationResponseDTO = new ClientRegistrationResponseDTO(
        clientRegistrationDTO,
        client.getClientId());

    Log.info("end");
    return Response.ok(clientRegistrationResponseDTO).build();
  }

  @PATCH
  @Path("/register/client_id/{client_id}")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Response updateClient(
      @Valid @ConvertGroup(to = ValidationGroups.PatchClient.class) ClientRegistrationDTO clientRegistrationDTOInput,
      @PathParam("client_id") String clientId) {
    Log.info("start");

    //1. Validate client infos
    clientRegistrationService.validateClientRegistrationInfo(
        clientRegistrationDTOInput);
    Log.info("client info validated successfully");

    //2. Retrieves client from db
    Client client = clientRegistrationService.getClientByClientId(clientId);

    //3. Check if userId in input matches the client userId on db
    ClientUtils.checkUserId(clientRegistrationDTOInput.getUserId(), client.getUserId());
    Log.info("client exists for clientId: " + clientId);

    //4. Convert client from db to ClientRegistrationDTO
    ClientRegistrationDTO clientRegistrationDTO =
        ClientUtils.convertClientToClientRegistrationDTO(client);

    //5. update clientRegistrationDTO from db with new fields of clientRegistrationDTOInput
    clientRegistrationService.patchClientRegistrationDTO(clientRegistrationDTOInput,
        clientRegistrationDTO);

    //6. Update client infos
    clientRegistrationService.updateClientRegistrationDTO(clientRegistrationDTO, clientId,
        client.getAttributeIndex(), client.getClientIdIssuedAt());
    Log.info("client updated successfully for clientId: " + clientId);

    //7. Prepare message for sns notification
    String message =
        "Name: " + clientRegistrationDTO.getClientName() + "\n" +
            "Client ID: " + clientId + "\n";
    boolean sendNotification = false;

    // Add information if redirectUris or metadata-related fields are updated
    if (clientRegistrationDTOInput.getRedirectUris() != null
        && !clientRegistrationDTOInput.getRedirectUris()
        .isEmpty()) {
      message += "- Redirect URIs updated \n";
      sendNotification = true;
    }
    Optional<String> updatedFields = getUpdateMessage(clientRegistrationDTOInput);
    if (updatedFields.isPresent()) {
      message += "- Metadata related fields updated: [" + updatedFields.get() + "]\n";
      sendNotification = true;
    }

    //8. Send SNS notification only if redirectUris or metadata-related fields has been updated
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
      @PathParam("client_id") String clientId, RefreshTokenRequestDTO refreshTokenRequestDTO) {
    String secret = clientRegistrationService.refreshClientSecret(
        clientId, refreshTokenRequestDTO.getUserId());
    Map<String, String> response = new HashMap<>();
    response.put("newClientSecret", secret);

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
