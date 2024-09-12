package it.pagopa.oneid.web;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification.S3EventNotificationRecord;
import it.pagopa.oneid.service.IDPMetadataServiceImpl;
import jakarta.inject.Inject;

public class OneIDLambdaUpdateIDPMetadata implements RequestHandler<S3Event, String> {

  @Inject
  IDPMetadataServiceImpl idpMetadataServiceImpl;

  @Override
  public String handleRequest(S3Event s3Event, Context context) {

    // Get notification record
    S3EventNotificationRecord record = s3Event.getRecords().getFirst();
    // Get file name
    String srcKey = record.getS3().getObject().getUrlDecodedKey();

    // Retrieve file content by file name
    String metadataContent = idpMetadataServiceImpl.getMetadataFile(srcKey);

    /* DummyController code*/

    // Update IDP Metadata table with parsed IDPMetadata information
    //idpMetadataServiceImpl.updateIDPMetadata(idpMetadataServiceImpl.parseIDPMetadata(metadataContent));

    return "Ok";
  }
}
