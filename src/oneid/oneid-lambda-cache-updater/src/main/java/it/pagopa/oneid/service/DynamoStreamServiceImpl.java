package it.pagopa.oneid.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.oneid.common.model.Client;
import it.pagopa.oneid.common.model.enums.AuthLevel;
import it.pagopa.oneid.common.model.enums.SamlBinding;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;

@ApplicationScoped
public class DynamoStreamServiceImpl implements DynamoStreamService {

  private static final String FIELD_CLIENT_ID = "clientId";
  private static final String FIELD_AUTH_LEVEL = "authLevel";
  private static final String FIELD_SAML_BINDING = "samlBinding";
  private static final String FIELD_DYNAMODB = "dynamodb";
  private static final String IMAGE_OLD = "OldImage";
  private static final String IMAGE_NEW = "NewImage";
  private static final List<DynamoField> CACHE_RELEVANT_SCALAR_FIELDS = List.of(
      new DynamoField("acsIndex", "N"),
      new DynamoField(FIELD_AUTH_LEVEL, "S"),
      new DynamoField(FIELD_SAML_BINDING, "S"),
      new DynamoField("attributeIndex", "N"),
      new DynamoField("active", "BOOL"),
      new DynamoField("requiredSameIdp", "BOOL"),
      new DynamoField("pairwise", "BOOL"),
      new DynamoField("spidMinors", "BOOL"),
      new DynamoField("spidProfessionals", "BOOL"),
      new DynamoField("minAge", "N"),
      new DynamoField("maxAge", "N"),
      new DynamoField("ageParentAuth", "N"),
      new DynamoField("friendlyName", "S")
  );
  private static final List<String> CACHE_RELEVANT_STRING_SET_FIELDS = List.of(
      "requestedParameters",
      "callbackURI"
  );

  private final ObjectMapper clientPayloadObjectMapper;

  @Inject
  DynamoStreamServiceImpl(ObjectMapper objectMapper) {
    this.clientPayloadObjectMapper = objectMapper.copy()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  @Override
  public Optional<Client> extractClient(JsonNode streamRecord, boolean preferOldImage) {
    return mapImageToClient(extractImage(streamRecord, preferOldImage));
  }

  @Override
  public Optional<String> extractClientId(JsonNode streamRecord, boolean preferOldImage) {
    JsonNode streamImage = extractImage(streamRecord, preferOldImage);
    if (streamImage == null || streamImage.isNull() || !streamImage.isObject()) {
      return Optional.empty();
    }

    String clientId = extractScalarFieldValue(streamImage.get(FIELD_CLIENT_ID), "S");
    if (StringUtils.isBlank(clientId)) {
      return Optional.empty();
    }

    return Optional.of(clientId);
  }

  @Override
  public boolean hasCacheRelevantChanges(JsonNode streamRecord) {
    if (streamRecord == null || streamRecord.isNull() || !streamRecord.isObject()) {
      return false;
    }

    if (!"MODIFY".equals(streamRecord.path("eventName").asText())) {
      return true;
    }

    JsonNode dynamodbNode = streamRecord.path(FIELD_DYNAMODB);
    if (dynamodbNode.isMissingNode() || dynamodbNode.isNull()) {
      return false;
    }

    JsonNode oldImage = dynamodbNode.path(IMAGE_OLD);
    JsonNode newImage = dynamodbNode.path(IMAGE_NEW);
    if (oldImage.isMissingNode() || oldImage.isNull() || newImage.isMissingNode()
        || newImage.isNull()) {
      return true;
    }

    for (DynamoField field : CACHE_RELEVANT_SCALAR_FIELDS) {
      if (hasDynamoScalarFieldChanged(oldImage, newImage, field.name(), field.type())) {
        return true;
      }
    }

    for (String fieldName : CACHE_RELEVANT_STRING_SET_FIELDS) {
      if (hasDynamoStringSetFieldChanged(oldImage, newImage, fieldName)) {
        return true;
      }
    }

    return false;
  }

  private JsonNode extractImage(JsonNode streamRecord, boolean preferOldImage) {
    if (streamRecord == null || streamRecord.isNull() || !streamRecord.isObject()) {
      return null;
    }

    JsonNode dynamodbNode = streamRecord.path(FIELD_DYNAMODB);
    if (dynamodbNode.isMissingNode() || dynamodbNode.isNull()) {
      return null;
    }

    JsonNode imageNode = preferOldImage ? dynamodbNode.path(IMAGE_OLD) : dynamodbNode.path(IMAGE_NEW);
    if (imageNode.isMissingNode() || imageNode.isNull()) {
      return null;
    }

    return imageNode;
  }

  private Optional<Client> mapImageToClient(JsonNode streamImage) {
    if (streamImage == null || streamImage.isNull() || !streamImage.isObject() || streamImage.size() == 0) {
      return Optional.empty();
    }

    Map<String, Object> payload = new HashMap<>();
    streamImage.fields().forEachRemaining(entry -> payload.put(entry.getKey(),
        fromDynamoAttribute(entry.getValue())));

    normalizeEnums(payload);

    try {
      Client client = clientPayloadObjectMapper.convertValue(payload, Client.class);
      validateClientPayload(client);
      return Optional.of(client);
    } catch (IllegalArgumentException conversionException) {
      throw new IllegalArgumentException("Unable to convert DynamoDB stream image to Client",
          conversionException);
    }
  }

  private void normalizeEnums(Map<String, Object> payload) {
    Object authLevel = payload.get(FIELD_AUTH_LEVEL);
    if (authLevel instanceof String authLevelValue && !authLevelValue.isBlank()) {
      AuthLevel parsedAuthLevel = AuthLevel.authLevelFromValue(authLevelValue);
      if (parsedAuthLevel == null) {
        throw new IllegalArgumentException("Unsupported authLevel value in stream image");
      }
      payload.put(FIELD_AUTH_LEVEL, parsedAuthLevel);
    }

    Object samlBinding = payload.get(FIELD_SAML_BINDING);
    if (samlBinding instanceof String samlBindingValue && !samlBindingValue.isBlank()) {
      payload.put(FIELD_SAML_BINDING, SamlBinding.samlBindingTypeFromValue(samlBindingValue));
    }
  }

  private void validateClientPayload(Client client) {
    if (StringUtils.isBlank(client.getClientId())) {
      throw new IllegalArgumentException("Stream payload without clientId");
    }

    if (client.getCallbackURI() == null || client.getCallbackURI().isEmpty()) {
      throw new IllegalArgumentException("Stream payload without callbackURI");
    }

    if (client.getRequestedParameters() == null || client.getRequestedParameters().isEmpty()) {
      throw new IllegalArgumentException("Stream payload without requestedParameters");
    }

    if (client.getAuthLevel() == null) {
      throw new IllegalArgumentException("Stream payload without authLevel");
    }
  }

  private boolean hasDynamoScalarFieldChanged(JsonNode oldImage,
                                              JsonNode newImage, String fieldName, String type) {
    String oldValue = extractScalarFieldValue(oldImage.get(fieldName), type);
    String newValue = extractScalarFieldValue(newImage.get(fieldName), type);
    return !Objects.equals(oldValue, newValue);
  }

  private boolean hasDynamoStringSetFieldChanged(JsonNode oldImage,
                                                 JsonNode newImage, String fieldName) {
    return !extractStringSetFieldValues(oldImage.get(fieldName))
        .equals(extractStringSetFieldValues(newImage.get(fieldName)));
  }

  private String extractScalarFieldValue(JsonNode fieldNode,
                                         String type) {
    if (fieldNode == null || fieldNode.isNull() || fieldNode.isMissingNode()) {
      return null;
    }

    JsonNode typedValue = fieldNode.get(type);
    if (typedValue == null || typedValue.isNull()) {
      return null;
    }

    String value = typedValue.asText();
    if (StringUtils.isBlank(value)) {
      return null;
    }

    return value;
  }

  private Set<String> extractStringSetFieldValues(JsonNode fieldNode) {
    Set<String> values = new HashSet<>();
    if (fieldNode == null || fieldNode.isNull() || fieldNode.isMissingNode()) {
      return values;
    }

    JsonNode stringSetValues = fieldNode.get("SS");
    if (stringSetValues == null || !stringSetValues.isArray()) {
      return values;
    }

    stringSetValues.forEach(value -> values.add(value.asText()));
    return values;
  }

  private Object fromDynamoAttribute(JsonNode attributeNode) {
    if (attributeNode == null || attributeNode.isNull() || attributeNode.isMissingNode()) {
      return null;
    }

    Object stringValue = readStringAttribute(attributeNode);
    if (stringValue != null) {
      return stringValue;
    }

    Object numberValue = readNumberAttribute(attributeNode);
    if (numberValue != null) {
      return numberValue;
    }

    Object booleanValue = readBooleanAttribute(attributeNode);
    if (booleanValue != null) {
      return booleanValue;
    }

    Object stringSetValue = readStringSetAttribute(attributeNode);
    if (stringSetValue != null) {
      return stringSetValue;
    }

    Object listValue = readListAttribute(attributeNode);
    if (listValue != null) {
      return listValue;
    }

    Object mapValue = readMapAttribute(attributeNode);
    if (mapValue != null) {
      return mapValue;
    }

    if (hasExplicitNullAttribute(attributeNode)) {
      return null;
    }

    return readFallbackAttributeValue(attributeNode);
  }

  private Object readStringAttribute(JsonNode attributeNode) {
    JsonNode stringValue = attributeNode.get("S");
    if (stringValue == null || stringValue.isNull()) {
      return null;
    }

    return stringValue.asText();
  }

  private Object readNumberAttribute(JsonNode attributeNode) {
    JsonNode numberValue = attributeNode.get("N");
    if (numberValue == null || numberValue.isNull()) {
      return null;
    }

    return parseNumeric(numberValue.asText());
  }

  private Object readBooleanAttribute(JsonNode attributeNode) {
    JsonNode booleanValue = attributeNode.get("BOOL");
    if (booleanValue == null || booleanValue.isNull()) {
      return null;
    }

    return booleanValue.asBoolean();
  }

  private Object readStringSetAttribute(JsonNode attributeNode) {
    JsonNode stringSetValues = attributeNode.get("SS");
    if (stringSetValues == null || !stringSetValues.isArray()) {
      return null;
    }

    Set<String> values = new HashSet<>();
    stringSetValues.forEach(value -> values.add(value.asText()));
    return values;
  }

  private Object readListAttribute(JsonNode attributeNode) {
    JsonNode listValues = attributeNode.get("L");
    if (listValues == null || !listValues.isArray()) {
      return null;
    }

    List<Object> values = new ArrayList<>();
    listValues.forEach(item -> values.add(fromDynamoAttribute(item)));
    return values;
  }

  private Object readMapAttribute(JsonNode attributeNode) {
    JsonNode mapValues = attributeNode.get("M");
    if (mapValues == null || !mapValues.isObject()) {
      return null;
    }

    Map<String, Object> map = new HashMap<>();
    mapValues.fields().forEachRemaining(entry -> map.put(entry.getKey(),
        fromDynamoAttribute(entry.getValue())));
    return map;
  }

  private boolean hasExplicitNullAttribute(JsonNode attributeNode) {
    JsonNode nullValue = attributeNode.get("NULL");
    return nullValue != null && nullValue.asBoolean(false);
  }

  private Object readFallbackAttributeValue(JsonNode attributeNode) {
    if (attributeNode.isTextual()) {
      return attributeNode.asText();
    }

    if (attributeNode.isNumber()) {
      return attributeNode.numberValue();
    }

    if (attributeNode.isBoolean()) {
      return attributeNode.asBoolean();
    }

    return null;
  }

  private Number parseNumeric(String numericValue) {
    if (numericValue.contains(".")) {
      return Double.parseDouble(numericValue);
    }
    return Long.parseLong(numericValue);
  }

  private record DynamoField(String name, String type) {
  }
}
