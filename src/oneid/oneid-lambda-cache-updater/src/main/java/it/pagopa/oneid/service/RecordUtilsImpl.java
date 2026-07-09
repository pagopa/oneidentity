package it.pagopa.oneid.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@ApplicationScoped
public class RecordUtilsImpl implements RecordUtils {
  private static final String RECORDS = "Records";
  private static final String RECORDS_LOWERCASE = "records";
  private static final String BODY = "body";

  private final ObjectMapper objectMapper;

  @Inject
  RecordUtilsImpl(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public List<JsonNode> readRecords(JsonNode root) {
    if (root == null || root.isNull()) {
      return List.of();
    }

    if (root.isArray()) {
      return readRecordArray(root);
    }

    if (root.has(RECORDS) && root.get(RECORDS).isArray()) {
      return readWrappedRecords(root.get(RECORDS));
    }

    if (root.has(RECORDS_LOWERCASE) && root.get(RECORDS_LOWERCASE).isArray()) {
      return readWrappedRecords(root.get(RECORDS_LOWERCASE));
    }

    return List.of(root);
  }

  private List<JsonNode> readWrappedRecords(JsonNode recordsNode) {
    if (recordsNode.size() == 0) {
      return List.of();
    }

    if (isSqsEnvelope(recordsNode)) {
      return StreamSupport.stream(recordsNode.spliterator(), false)
          .flatMap(this::readSqsMessageBody)
          .toList();
    }

    return readRecordArray(recordsNode);
  }

  private boolean isSqsEnvelope(JsonNode recordsNode) {
    JsonNode firstRecord = recordsNode.get(0);
    return firstRecord != null && firstRecord.isObject() && firstRecord.has(BODY);
  }

  private Stream<JsonNode> readSqsMessageBody(JsonNode sqsRecord) {
    JsonNode bodyNode = sqsRecord.get(BODY);
    if (bodyNode == null || bodyNode.isNull()) {
      return Stream.empty();
    }

    if (!bodyNode.isTextual()) {
      return readRecords(bodyNode).stream();
    }

    try {
      JsonNode body = objectMapper.readTree(bodyNode.asText());
      return readRecords(body).stream();
    } catch (JsonProcessingException parsingException) {
      throw new IllegalArgumentException("Unable to parse SQS message body", parsingException);
    }
  }

  private List<JsonNode> readRecordArray(JsonNode recordsNode) {
    return StreamSupport.stream(recordsNode.spliterator(), false).toList();
  }

  @Override
  public boolean isIncompleteModifyRecord(JsonNode streamRecord) {
    JsonNode dynamodbNode = streamRecord.path("dynamodb");
    if (dynamodbNode.isMissingNode() || dynamodbNode.isNull()) {
      return true;
    }

    JsonNode oldImageNode = dynamodbNode.path("OldImage");
    JsonNode newImageNode = dynamodbNode.path("NewImage");
    return oldImageNode.isMissingNode() || oldImageNode.isNull()
        || newImageNode.isMissingNode() || newImageNode.isNull();
  }
}
