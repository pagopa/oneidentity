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

  Map<String, String> envShortToLong = new HashMap<>() {{
    put("d", "dev");
    put("u", "uat");
    put("p", "prod");
  }};


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

    String message =
        "**Name**: " + clientRegistrationResponseDTO.getClientName() + "\n" +
            "**Client ID**: " + clientRegistrationResponseDTO.getClientID() + "\n" +
            "**Attributes**: " + clientRegistrationResponseDTO.getSamlRequestedAttributes() + "\n" +
            "**Redirect URIs**: " + clientRegistrationResponseDTO.getRedirectUris();

    String subject = "New Client registered in " + envShortToLong.get(environment);
    try {
      sns.publish(p ->
          p.topicArn(topicArn).subject(subject).message(message));
    } catch (Exception e) {
      Log.error("Failed to send SNS notification: ", e);
    }

    Log.info("end");

    return Response.ok(clientRegistrationResponseDTO).status(Status.CREATED).build();

  }

  @GET
  @Path("/register/{client_id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getClientInfoByClientId(@PathParam("client_id") String clientId) {
    return Response.ok(clientRegistrationService.getClientMetadataDTO(
        clientId)).build();
  }

}
