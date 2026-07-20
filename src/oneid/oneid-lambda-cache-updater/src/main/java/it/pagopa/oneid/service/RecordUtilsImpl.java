package it.pagopa.oneid.service;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.stream.StreamSupport;

@ApplicationScoped
public class RecordUtilsImpl implements RecordUtils {
  private static final String RECORDS = "Records";
  private static final String RECORDS_LOWERCASE = "records";

  @Override
  public List<JsonNode> readRecords(JsonNode root) {
    if (root == null || root.isNull()) {
      return List.of();
    }

    if (root.isArray()) {
      return readRecordArray(root);
    }

    if (root.has(RECORDS) && root.get(RECORDS).isArray()) {
      return readRecordArray(root.get(RECORDS));
    }

    if (root.has(RECORDS_LOWERCASE) && root.get(RECORDS_LOWERCASE).isArray()) {
      return readRecordArray(root.get(RECORDS_LOWERCASE));
    }

    return List.of(root);
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

  private List<JsonNode> readRecordArray(JsonNode recordsNode) {
    return StreamSupport.stream(recordsNode.spliterator(), false).toList();
  }
}
