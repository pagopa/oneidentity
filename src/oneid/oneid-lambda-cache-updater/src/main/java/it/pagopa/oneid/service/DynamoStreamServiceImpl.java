package it.pagopa.oneid.service;

import com.amazonaws.services.lambda.runtime.events.DynamodbEvent.DynamodbStreamRecord;
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue;
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.StreamRecord;
import com.fasterxml.jackson.databind.DeserializationFeature;
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
  private static final List<String> CACHE_RELEVANT_SCALAR_FIELDS = List.of(
      "acsIndex",
      FIELD_AUTH_LEVEL,
      FIELD_SAML_BINDING,
      "attributeIndex",
      "active",
      "requiredSameIdp",
      "pairwise",
      "spidMinors",
      "spidProfessionals",
      "minAge",
      "maxAge",
      "ageParentAuth",
      "friendlyName"
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
  public Optional<Client> extractClient(DynamodbStreamRecord streamRecord, boolean preferOldImage) {
    return mapImageToClient(extractImage(streamRecord, preferOldImage));
  }

  @Override
  public Optional<String> extractClientId(DynamodbStreamRecord streamRecord, boolean preferOldImage) {
    Map<String, AttributeValue> image = extractImage(streamRecord, preferOldImage);
    if (image == null) {
      return Optional.empty();
    }

    String clientId = extractScalarString(image.get(FIELD_CLIENT_ID));
    if (StringUtils.isBlank(clientId)) {
      return Optional.empty();
    }

    return Optional.of(clientId);
  }

  @Override
  public boolean hasCacheRelevantChanges(DynamodbStreamRecord streamRecord) {
    if (streamRecord == null) {
      return false;
    }

    if (!"MODIFY".equals(streamRecord.getEventName())) {
      return true;
    }

    StreamRecord dynamodb = streamRecord.getDynamodb();
    if (dynamodb == null) {
      return false;
    }

    Map<String, AttributeValue> oldImage = dynamodb.getOldImage();
    Map<String, AttributeValue> newImage = dynamodb.getNewImage();
    if (oldImage == null || newImage == null) {
      return true;
    }

    for (String fieldName : CACHE_RELEVANT_SCALAR_FIELDS) {
      if (hasScalarFieldChanged(oldImage, newImage, fieldName)) {
        return true;
      }
    }

    for (String fieldName : CACHE_RELEVANT_STRING_SET_FIELDS) {
      if (hasStringSetFieldChanged(oldImage, newImage, fieldName)) {
        return true;
      }
    }

    return false;
  }

  private Map<String, AttributeValue> extractImage(DynamodbStreamRecord streamRecord,
                                                    boolean preferOldImage) {
    if (streamRecord == null) {
      return null;
    }

    StreamRecord dynamodb = streamRecord.getDynamodb();
    if (dynamodb == null) {
      return null;
    }

    Map<String, AttributeValue> image = preferOldImage
        ? dynamodb.getOldImage()
        : dynamodb.getNewImage();
    if (image == null || image.isEmpty()) {
      return null;
    }

    return image;
  }

  private Optional<Client> mapImageToClient(Map<String, AttributeValue> image) {
    if (image == null || image.isEmpty()) {
      return Optional.empty();
    }

    Map<String, Object> payload = new HashMap<>();
    image.forEach((key, value) -> payload.put(key, fromAttributeValue(value)));

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

  private boolean hasScalarFieldChanged(Map<String, AttributeValue> oldImage,
                                         Map<String, AttributeValue> newImage, String fieldName) {
    return !Objects.equals(
        extractScalarString(oldImage.get(fieldName)),
        extractScalarString(newImage.get(fieldName)));
  }

  private boolean hasStringSetFieldChanged(Map<String, AttributeValue> oldImage,
                                            Map<String, AttributeValue> newImage,
                                            String fieldName) {
    return !extractStringSet(oldImage.get(fieldName))
        .equals(extractStringSet(newImage.get(fieldName)));
  }

  private String extractScalarString(AttributeValue attributeValue) {
    if (attributeValue == null) {
      return null;
    }

    if (attributeValue.getS() != null) {
      return StringUtils.isBlank(attributeValue.getS()) ? null : attributeValue.getS();
    }

    if (attributeValue.getN() != null) {
      return attributeValue.getN();
    }

    if (attributeValue.getBOOL() != null) {
      return String.valueOf(attributeValue.getBOOL());
    }

    return null;
  }

  private Set<String> extractStringSet(AttributeValue attributeValue) {
    if (attributeValue == null || attributeValue.getSS() == null) {
      return Set.of();
    }
    return new HashSet<>(attributeValue.getSS());
  }

  private Object fromAttributeValue(AttributeValue attributeValue) {
    if (attributeValue == null) {
      return null;
    }

    if (Boolean.TRUE.equals(attributeValue.getNULL())) {
      return null;
    }

    if (attributeValue.getS() != null) {
      return attributeValue.getS();
    }

    if (attributeValue.getN() != null) {
      return parseNumeric(attributeValue.getN());
    }

    if (attributeValue.getBOOL() != null) {
      return attributeValue.getBOOL();
    }

    if (attributeValue.getSS() != null) {
      return new HashSet<>(attributeValue.getSS());
    }

    if (attributeValue.getL() != null) {
      List<Object> list = new ArrayList<>();
      attributeValue.getL().forEach(item -> list.add(fromAttributeValue(item)));
      return list;
    }

    if (attributeValue.getM() != null) {
      Map<String, Object> map = new HashMap<>();
      attributeValue.getM().forEach((k, v) -> map.put(k, fromAttributeValue(v)));
      return map;
    }

    return null;
  }

  private Number parseNumeric(String numericValue) {
    if (numericValue.contains(".")) {
      return Double.parseDouble(numericValue);
    }
    return Long.parseLong(numericValue);
  }
}
