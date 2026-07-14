package it.pagopa.oneid.web;

import java.util.ArrayList;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification.S3EventNotificationRecord;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
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
    Log.debug("start");
    Log.info("IDP metadata Lambda handler invoked, requestId="
      + (context == null ? "unknown" : context.getAwsRequestId()));

    if (input == null || !input.isObject() || !input.path("Records").isArray()
      || input.path("Records").isEmpty()) {
      Log.warn("Ignoring Lambda event without records");
      return "Ignored";
    }

    //TODO remove
    Log.info("\n\nInput event: " + input.toString());

    String eventSource = input.path("Records").get(0).path("eventSource").asText();
    Log.info("Processing IDP metadata event: source=" + eventSource + ", records="
      + input.path("Records").size());
    return switch (eventSource) {
      case "aws:s3" -> handleS3Event(objectMapper.convertValue(input, S3Event.class));
      case "aws:dynamodb" -> handleDynamodbEvent(objectMapper.copy()
          .setConfig(objectMapper.getDeserializationConfig()
              .with(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES))
          .convertValue(input, DynamodbEvent.class));
      default -> throw new IllegalArgumentException("Unsupported event source: " + eventSource);
    };
  }

  private String handleS3Event(S3Event s3Event) {
    Log.debug("start");

    //TODO remove
    Log.info("\n\nS3 event: " + s3Event.toString());

    // Get notification record
    S3EventNotificationRecord record = s3Event.getRecords().getFirst();
    // Get file name
    String srcKey = record.getS3().getObject().getUrlDecodedKey();
    Log.info("Processing metadata file from S3: key=" + srcKey);

    // Retrieve file content by file name
    String metadataContent = idpMetadataServiceImpl.getMetadataFile(srcKey);

    IdpS3FileDTO idpS3FileDTO = new IdpS3FileDTO(srcKey);
    ArrayList<IDP> idpMetadata = idpMetadataServiceImpl.parseIDPMetadata(metadataContent,
      idpS3FileDTO);

    // Update IDP Metadata table with parsed IDPMetadata information
    idpMetadataServiceImpl.updateIDPMetadata(idpMetadata, idpS3FileDTO);
    // Publish the public SPID IDPs to S3
    idpMetadataServiceImpl.publishPublicIdps(idpMetadata, idpS3FileDTO);
    Log.info("S3 metadata event processed: key=" + srcKey + ", parsedIdps="
      + idpMetadata.size());
    return "Ok";
  }

  private String handleDynamodbEvent(DynamodbEvent dynamodbEvent) {
    Log.debug("start");

    //TODO remove
    Log.info("\n\nDynamoDB event: " + dynamodbEvent.toString());
    
    if (dynamodbEvent == null || dynamodbEvent.getRecords() == null) {
      Log.warn("Ignoring DynamoDB event without records");
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
      Log.info("DynamoDB status change detected for public IDPs; refreshing S3 snapshot");
      idpMetadataServiceImpl.refreshPublicIdps();
    } else {
      Log.info("No public IDPs status change detected; S3 snapshot was not refreshed");
    }

    return "Ok";
  }
}
