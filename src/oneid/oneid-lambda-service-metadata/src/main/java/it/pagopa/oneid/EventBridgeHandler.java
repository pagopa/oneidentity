package it.pagopa.oneid;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.ScheduledEvent;
import it.pagopa.oneid.common.utils.logging.CustomLogging;
import jakarta.inject.Inject;

@CustomLogging
public class EventBridgeHandler implements RequestHandler<ScheduledEvent, String> {

  @Inject
  MetadataUtils metadataUtils;

  @Override
  public String handleRequest(ScheduledEvent event, Context context) {

    metadataUtils.processMetadataAndUpload();

    return "SPID and CIE metadata uploaded successfully";
  }


}
