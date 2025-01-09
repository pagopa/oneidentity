package it.pagopa.oneid;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent.DynamodbStreamRecord;
import it.pagopa.oneid.common.utils.logging.CustomLogging;
import jakarta.inject.Inject;
import java.util.List;

@CustomLogging
public class DynamoDbHandler implements RequestHandler<DynamodbEvent, String> {

  @Inject
  MetadataUtils metadataUtils;


  @Override
  public String handleRequest(DynamodbEvent event, Context context) {

    for (DynamodbStreamRecord record : event.getRecords()) {
      if (record.getEventName().equals("MODIFY") && !hasMetadataChanged(record)) {
        return "SPID and CIE metadata didn't change";
      }
      metadataUtils.processMetadataAndUpload();
    }

    return "SPID and CIE metadata uploaded successfully";
  }

  private boolean hasMetadataChanged(DynamodbStreamRecord record) {

    String acsIndexOld = record.getDynamodb().getOldImage().get("acsIndex").getN();
    String acsIndexNew = record.getDynamodb().getNewImage().get("acsIndex").getN();
    if (!acsIndexNew.equals(acsIndexOld)) {
      return true;
    }
    String friendlyNameOld = record.getDynamodb().getOldImage().get("friendlyName").getS();
    String friendlyNameNew = record.getDynamodb().getNewImage().get("friendlyName").getS();
    if (!friendlyNameNew.equals(friendlyNameOld)) {
      return true;
    }

    List<String> requestedParametersOld = record.getDynamodb().getOldImage()
        .get("requestedParameters").getSS();
    List<String> requestedParametersNew = record.getDynamodb().getNewImage()
        .get("requestedParameters").getSS();
    if (!requestedParametersNew.equals(requestedParametersOld)) {
      return true;
    }
    String authLevelOld = record.getDynamodb().getOldImage().get("authLevel").getS();
    String authLevelNew = record.getDynamodb().getNewImage().get("authLevel").getS();
    if (!authLevelNew.equals(authLevelOld)) {
      return true;
    }
    String attributeIndexOld = record.getDynamodb().getOldImage().get("attributeIndex")
        .getN();
    String attributeIndexNew = record.getDynamodb().getNewImage().get("attributeIndex")
        .getN();
    if (!attributeIndexNew.equals(attributeIndexOld)) {
      return true;
    }
    boolean isActiveOld = record.getDynamodb().getOldImage().get("active").getBOOL();
    boolean isActiveNew = record.getDynamodb().getNewImage().get("active").getBOOL();
    return isActiveNew != isActiveOld;
  }

}
