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
import java.util.regex.PatternSyntaxException;

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
    JsonNode jsonNode;
    JsonNode s3Node;

    try {
      jsonNode = objectMapper.readTree(snsMessage).get("Records");
      s3Node = jsonNode.get(0).get("s3");
    } catch (JsonProcessingException e) {
      Log.error("Error processing SNS message: " + snsMessage);
      throw new RuntimeException(e);
    }

    String timestamp;
    String idpType;

    JsonNode objectNode;
    String s3File;
    String s3Key;

    try {
      objectNode = s3Node.get("object");
      s3Key = objectNode.get("key").asText();
    } catch (NullPointerException e) {
      Log.error("Error processing SNS message: " + snsMessage);
      throw new RuntimeException(e);
    }
    if (s3Key == null) {
      Log.error("No 'object' field found in the SNS message.");
      throw new RuntimeException();
    }

    try {
      s3File = s3Key.split("history/")[1];
      idpType = s3File.split("-")[0].replace(".xml", "");
      timestamp = s3File.split("-")[1];

    } catch (PatternSyntaxException e) {
      Log.error("Error parsing s3Key: " + s3Key);
      throw new RuntimeException(e);
    }

    // 2. Download metadata from IS
    String metadataContent = isServiceImpl.getLatestIdpMetadata(idpType, timestamp);

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