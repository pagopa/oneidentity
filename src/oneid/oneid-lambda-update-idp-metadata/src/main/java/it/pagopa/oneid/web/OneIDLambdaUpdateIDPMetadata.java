package it.pagopa.oneid.web;

import java.util.ArrayList;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification.S3EventNotificationRecord;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.logging.Log;
import it.pagopa.oneid.common.model.IDP;
import it.pagopa.oneid.common.model.dto.IdpS3FileDTO;
import it.pagopa.oneid.service.IDPMetadataServiceImpl;
import jakarta.inject.Inject;

public class OneIDLambdaUpdateIDPMetadata implements RequestHandler<JsonNode, String> {

  @Inject
  IDPMetadataServiceImpl idpMetadataServiceImpl;

  @Inject
  ObjectMapper objectMapper;

  @Override
  public String handleRequest(JsonNode input, Context context) {
    //TODO remove after tests
    Log.infof("IDP metadata Lambda handler invoked, requestId=%s",
        context == null ? "unknown" : context.getAwsRequestId());

    if (input == null || !input.path("Records").isArray() || input.path("Records").isEmpty()) {
      return "Ignored";
    }

    String eventSource = input.path("Records").get(0).path("eventSource").asText();
    return switch (eventSource) {
      case "aws:s3" -> handleS3Event(objectMapper.convertValue(input, S3Event.class));
      case "aws:dynamodb" -> handleDynamodbEvent(objectMapper.convertValue(input,
          DynamodbEvent.class));
      default -> throw new IllegalArgumentException("Unsupported event source: " + eventSource);
    };
  }

  private String handleS3Event(S3Event s3Event) {
    // Get notification record
    S3EventNotificationRecord record = s3Event.getRecords().getFirst();
    // Get file name
    String srcKey = record.getS3().getObject().getUrlDecodedKey();

    // Retrieve file content by file name
    String metadataContent = idpMetadataServiceImpl.getMetadataFile(srcKey);

    IdpS3FileDTO idpS3FileDTO = new IdpS3FileDTO(srcKey);
    ArrayList<IDP> idpMetadata = idpMetadataServiceImpl.parseIDPMetadata(metadataContent,
      idpS3FileDTO);

    // Update IDP Metadata table with parsed IDPMetadata information
    idpMetadataServiceImpl.updateIDPMetadata(idpMetadata, idpS3FileDTO);
    // Publish the public SPID IDPs to S3
    idpMetadataServiceImpl.publishPublicIdps(idpMetadata, idpS3FileDTO);
    return "Ok";
  }

  private String handleDynamodbEvent(DynamodbEvent dynamodbEvent) {
    if (dynamodbEvent == null || dynamodbEvent.getRecords() == null) {
      return "Ignored";
    }

    boolean publicIdpsStatusChanged = false;
    for (DynamodbEvent.DynamodbStreamRecord record : dynamodbEvent.getRecords()) {
      // Validate the raw status before the model converter normalizes it
        idpMetadataServiceImpl.validateDynamodbStatus(record);

      // Detect changes that affect the IDPs snapshot
      publicIdpsStatusChanged = publicIdpsStatusChanged
          || idpMetadataServiceImpl.isPublicIdpsStatusChange(record);
    }

    if (publicIdpsStatusChanged) {
      // Update the IDPs snapshot in S3
      idpMetadataServiceImpl.refreshPublicIdps();
    }

    return "Ok";
  }
}
