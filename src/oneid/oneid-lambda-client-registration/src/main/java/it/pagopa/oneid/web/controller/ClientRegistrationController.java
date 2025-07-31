package it.pagopa.oneid.web.controller;

import io.quarkus.logging.Log;
import it.pagopa.oneid.model.dto.ClientRegistrationDTO;
import it.pagopa.oneid.model.dto.ClientRegistrationResponseDTO;
import it.pagopa.oneid.model.enums.EnvironmentMapping;
import it.pagopa.oneid.model.groups.ValidationGroups;
import it.pagopa.oneid.service.ClientRegistrationServiceImpl;
import it.pagopa.oneid.web.dto.RefreshTokenRequestDTO;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.groups.ConvertGroup;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import java.util.HashMap;
import java.util.Map;
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
      @Valid @ConvertGroup(to = ValidationGroups.Registration.class) ClientRegistrationDTO clientRegistrationDTOInput) {
    Log.info("start");

    clientRegistrationService.validateClientRegistrationInfo(clientRegistrationDTOInput);

    Log.info("client info validated successfully");

    ClientRegistrationResponseDTO clientRegistrationResponseDTO = clientRegistrationService.saveClient(
        clientRegistrationDTOInput);
    Log.info(
        "client saved successfully with clientId: " + clientRegistrationResponseDTO.getClientID());

    String message =
        "Name: " + clientRegistrationResponseDTO.getClientName() + "\n" +
            "Client ID: " + clientRegistrationResponseDTO.getClientID() + "\n" +
            "Attributes: " + clientRegistrationResponseDTO.getSamlRequestedAttributes().stream()
            .map(Enum::name).toList() + "\n" +
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
  @Path("/register/{client_id}/{user_id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getClientInfoByClientId(
      @PathParam("client_id") String clientId,
      @PathParam("user_id") String userId) {
    Log.info("start");

    //1. Verify if client exists and if so retrieves it from db
    ClientRegistrationDTO clientRegistrationDTOresponse = clientRegistrationService.getClientRegistrationDTO(
        clientId, userId);

    Log.info("end");
    return Response.ok(clientRegistrationDTOresponse).build();
  }

  @PUT
  @Path("/register/{client_id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response updateClient(
      @Valid @ConvertGroup(to = ValidationGroups.PutClient.class) ClientRegistrationDTO clientRegistrationDTOInput,
      @PathParam("client_id") String clientId) {
    Log.info("start");

    //1. Validate client infos
    clientRegistrationService.validateClientRegistrationInfo(
        clientRegistrationDTOInput);
    Log.info("client info validated successfully");

    //2. Verify if client exists and if so retrieves it from db
    ClientRegistrationDTO clientRegistrationDTO = clientRegistrationService.getClientRegistrationDTO(
        clientId, clientRegistrationDTOInput.getUserId());
    Log.info("client exists for clientId: " + clientId);

    //3. Update client infos
    clientRegistrationService.updateClientRegistrationDTO(
        clientRegistrationDTOInput);
    Log.info("client updated successfully for userId: " + clientId);

    //4. Send SNS notification
    String message =
        "Name: " + clientRegistrationDTO.getClientName() + "\n" +
            "Client ID: " + clientId + "\n";
    String subject =
        "Client updated in " + EnvironmentMapping.valueOf(environment).getEnvLong();
    try {
      sns.publish(p ->
          p.topicArn(topicArn).subject(subject).message(message));
    } catch (Exception e) {
      Log.log(EnvironmentMapping.valueOf(environment).getLogLevel(),
          "Failed to send SNS notification: ", e);
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
