package it.pagopa.oneid.web;

import static it.pagopa.oneid.utils.Constants.BRANCH_BASE_NAME;
import static it.pagopa.oneid.utils.Constants.METADATA_BASE_PATH;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.amazonaws.services.lambda.runtime.events.SNSEvent.SNSRecord;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.logging.Log;
import it.pagopa.oneid.service.GitHubServiceImpl;
import it.pagopa.oneid.service.ISServiceImpl;
import jakarta.inject.Inject;

public class ISGHIntegration implements RequestHandler<SNSEvent, String> {

  @Inject
  ISServiceImpl isServiceImpl;

  @Inject
  GitHubServiceImpl gitHubServiceImpl;


  @Override
  public String handleRequest(SNSEvent event, Context context) {
    //TODO add exceptions handling

    ObjectMapper objectMapper = new ObjectMapper();
    String snsMessage;

    SNSRecord record = event.getRecords().getFirst();

    // 1. Read SNS event
    snsMessage = record.getSNS().getMessage();
    String timestamp;
    String idpType;

    JsonNode dataNode;
    try {
      dataNode = objectMapper.readTree(snsMessage).get("data");
    } catch (JsonProcessingException e) {
      Log.error("Error processing SNS message: " + snsMessage);
      throw new RuntimeException(e);
    }
    if (dataNode == null) {
      Log.error("No 'data' field found in the SNS message.");
      throw new RuntimeException();
    }

    timestamp = dataNode.get("TAG").asText();
    idpType = dataNode.get("OBJ").asText();

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

    return snsMessage;
  }
}