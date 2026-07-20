package it.pagopa.oneid.web;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.logging.Log;
import it.pagopa.oneid.common.model.IDP;
import it.pagopa.oneid.common.model.dto.IdpS3FileDTO;
import it.pagopa.oneid.service.IDPMetadataServiceImpl;
import jakarta.inject.Inject;

public class OneIDLambdaUpdateIDPMetadata implements RequestHandler<Object, String> {

  private static final String EVENT_SOURCE = "eventSource";
  private static final String IGNORED = "Ignored";
  private static final String RECORDS = "Records";

  @Inject
  IDPMetadataServiceImpl idpMetadataServiceImpl;

  @Inject
  ObjectMapper objectMapper;

  @Override
  public String handleRequest(Object event, Context context) {

    JsonNode input = objectMapper.valueToTree(event);
    if (input == null || !input.isObject()) {
      Log.warn("Ignoring Lambda event without records");
      return IGNORED;
    }

    JsonNode records = input.path(RECORDS);
    if (!records.isArray() || records.isEmpty()) {
      Log.warn("Ignoring Lambda event without records");
      return IGNORED;
    }

    String eventSource = records.get(0).path(EVENT_SOURCE).asText();
    Log.info("Processing IDP metadata event: source=" + eventSource + ", records="
      + records.size());
    return switch (eventSource) {
      case "aws:s3" -> handleS3Event(records.get(0));
      case "aws:dynamodb" -> handleDynamodbEvent(records);
      default -> throw new IllegalArgumentException("Unsupported event source: " + eventSource);
    };
  }

  private String handleS3Event(JsonNode s3EventRecord) {

    if (!"aws:s3".equals(s3EventRecord.path(EVENT_SOURCE).asText())) {
      Log.warn("Ignoring non-S3 record in S3 event");
      return IGNORED;
    }

    String encodedKey = s3EventRecord.path("s3").path("object").path("key").asText();
    if (encodedKey.isBlank()) {
      Log.warn("Ignoring S3 event without object key");
      return IGNORED;
    }

    String srcKey = URLDecoder.decode(encodedKey, StandardCharsets.UTF_8);
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

  private String handleDynamodbEvent(JsonNode records) {

    if (!records.isArray() || records.isEmpty()) {
      Log.warn("Ignoring DynamoDB event without records");
      return IGNORED;
    }

    boolean publicIdpsStatusChanged = false;
    for (JsonNode dynamodbEventRecord : records) {
      if (!"aws:dynamodb".equals(dynamodbEventRecord.path(EVENT_SOURCE).asText())) {
        Log.warn("Ignoring non-DynamoDB record in DynamoDB event");
        continue;
      }

      // Detect changes that affect the IDPs snapshot
      publicIdpsStatusChanged = publicIdpsStatusChanged
          || idpMetadataServiceImpl.isPublicIdpsStatusChange(dynamodbEventRecord);
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
