package it.pagopa.oneid.web;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.amazonaws.services.lambda.runtime.events.SNSEvent.SNSRecord;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.logging.Log;
import it.pagopa.oneid.service.GitHubServiceImpl;
import it.pagopa.oneid.service.ISServiceImpl;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

public class ISGHIntegration implements RequestHandler<SNSEvent, String> {

  @Inject
  ISServiceImpl isServiceImpl;

  @Inject
  GitHubServiceImpl gitHubServiceImpl;


  @ConfigProperty(name = "metadata_base_path")
  String METADATA_BASE_PATH;
  @ConfigProperty(name = "branch_base_name")
  String BRANCH_BASE_NAME;

  @Override
  public String handleRequest(SNSEvent event, Context context) {
    //TODO add exceptions handling

    ObjectMapper objectMapper = new ObjectMapper();
    String snsMessage = null;

    for (SNSRecord record : event.getRecords()) {

      // 1. Read SNS event
      snsMessage = record.getSNS().getMessage();
      String timestamp;
      String idpType;
      try {
        JsonNode dataNode = objectMapper.readTree(snsMessage).get("data");

        if (dataNode != null) {
          timestamp = dataNode.get("TAG").asText();
          idpType = dataNode.get("OBJ").asText();
        } else {
          Log.error("No 'data' field found in the SNS message.");
          throw new RuntimeException();
        }

      } catch (Exception e) {
        Log.error("Error processing SNS message:   " + snsMessage);
        throw new RuntimeException();
      }

      // 2. Download metadata from IS
      String metadataContent = isServiceImpl.getLatestIdpMetadata(idpType);

      // 3. Interact with GitHub Repository
      String branchName = BRANCH_BASE_NAME + idpType + "-" + timestamp;
      String metadataPath = METADATA_BASE_PATH + idpType + "-" + timestamp + ".xml";
      String prTitle = "feat: Update " + idpType + "-" + timestamp + ".xml";

      gitHubServiceImpl.openPullRequest(prTitle,
          "main",
          branchName,
          metadataContent,
          metadataPath,
          idpType);
    }

    return snsMessage;
  }
}