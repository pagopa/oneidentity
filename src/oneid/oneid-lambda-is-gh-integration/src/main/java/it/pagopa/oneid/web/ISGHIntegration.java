package it.pagopa.oneid.web;

import static it.pagopa.oneid.utils.Constants.BRANCH_BASE_NAME;
import static it.pagopa.oneid.utils.Constants.METADATA_BASE_PATH;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification.S3EventNotificationRecord;
import io.quarkus.logging.Log;
import it.pagopa.oneid.service.GitHubServiceImpl;
import it.pagopa.oneid.service.ISServiceImpl;
import jakarta.inject.Inject;
import java.util.regex.PatternSyntaxException;

public class ISGHIntegration implements RequestHandler<S3Event, String> {

  @Inject
  ISServiceImpl isServiceImpl;

  @Inject
  GitHubServiceImpl gitHubServiceImpl;


  @Override
  public String handleRequest(S3Event event, Context context) {
    //TODO add exceptions handling

    String s3Key;
    String s3File;

    S3EventNotificationRecord record = event.getRecords().getFirst();

    // 1. Read S3 event
    Log.debug(record);
    s3Key = record.getS3().getObject().getKey();
    String timestamp;
    String idpType;

    try {
      s3File = s3Key.split("history/")[1];
      idpType = s3File.split("-")[0].replace(".xml", "");
      timestamp = s3File.split("-")[1];

    } catch (PatternSyntaxException e) {
      Log.error("Error parsing s3Key: " + s3Key);
      throw new RuntimeException(e);
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

    return s3File;
  }
}