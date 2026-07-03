package it.pagopa.oneid.service;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.stream.StreamSupport;

@ApplicationScoped
public class RecordUtilsImpl implements RecordUtils {
  @Override
  public List<JsonNode> readRecords(JsonNode root) {
    if (root == null || root.isNull()) {
      return List.of();
    }

    if (root.isArray()) {
      return readRecordArray(root);
    }

    if (root.has("Records") && root.get("Records").isArray()) {
      return readRecordArray(root.get("Records"));
    }

    if (root.has("records") && root.get("records").isArray()) {
      return readRecordArray(root.get("records"));
    }

    return List.of(root);
  }

  private List<JsonNode> readRecordArray(JsonNode recordsNode) {
    return StreamSupport.stream(recordsNode.spliterator(), false).toList();
  }

  @Override
  public boolean isIncompleteModifyRecord(JsonNode record) {
    JsonNode dynamodbNode = record.path("dynamodb");
    if (dynamodbNode.isMissingNode() || dynamodbNode.isNull()) {
      return true;
    }

    JsonNode oldImageNode = dynamodbNode.path("OldImage");
    JsonNode newImageNode = dynamodbNode.path("NewImage");
    return oldImageNode.isMissingNode() || oldImageNode.isNull()
        || newImageNode.isMissingNode() || newImageNode.isNull();
  }
}
