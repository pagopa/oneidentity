package it.pagopa.oneid.common.model.converters;

import io.quarkus.logging.Log;
import it.pagopa.oneid.common.model.LocalizedContent;
import java.util.HashMap;
import java.util.Map;
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.enhanced.dynamodb.internal.converter.attribute.MapAttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.internal.converter.string.StringStringConverter;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public class HashMapAttributeConverter implements
    AttributeConverter<Map<String, Map<String, LocalizedContent>>> {

  private static AttributeConverter<Map<String, Map<String, LocalizedContent>>> mapConverter;

  public HashMapAttributeConverter() {
    mapConverter =
        MapAttributeConverter.builder(
                EnhancedType.mapOf(
                    EnhancedType.of(String.class),
                    EnhancedType.mapOf(String.class, LocalizedContent.class)))
            .mapConstructor(HashMap::new)
            .keyConverter(StringStringConverter.create())
            .valueConverter(
                MapAttributeConverter.builder(
                        EnhancedType.mapOf(String.class, LocalizedContent.class))
                    .mapConstructor(HashMap::new)
                    .keyConverter(StringStringConverter.create())
                    .valueConverter(new LocalizedContentAttributeConverter())
                    .build())
            .build();
  }

  @Override
  public AttributeValue transformFrom(Map<String, Map<String, LocalizedContent>> map) {
    return mapConverter.transformFrom(map);
  }

  @Override
  public Map<String, Map<String, LocalizedContent>> transformTo(AttributeValue attributeValue) {
    Map<String, Map<String, LocalizedContent>> map;
    try {
      map = mapConverter.transformTo(attributeValue);
    } catch (RuntimeException e) {
      Log.error("Failed to convert attribute value to map", e);
      map = new HashMap<>();
    }

    return map;
  }

  @Override
  public EnhancedType<Map<String, Map<String, LocalizedContent>>> type() {
    return mapConverter.type();
  }

  @Override
  public AttributeValueType attributeValueType() {
    return mapConverter.attributeValueType();
  }
}