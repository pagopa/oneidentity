package it.pagopa.oneid.common.model.converters;

import io.quarkus.logging.Log;
import it.pagopa.oneid.common.model.LocalizedContent;
import it.pagopa.oneid.common.model.LocalizedContentMap;
import java.util.HashMap;
import java.util.Map;
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.enhanced.dynamodb.internal.converter.attribute.MapAttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.internal.converter.string.StringStringConverter;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public class LocalizedContentMapAttributeConverter implements
    AttributeConverter<LocalizedContentMap> {

  private static AttributeConverter<Map<String, Map<String, LocalizedContent>>> mapConverter;

  public LocalizedContentMapAttributeConverter() {
    mapConverter =
        MapAttributeConverter.builder(
                EnhancedType.mapOf(
                    EnhancedType.of(String.class),
                    EnhancedType.mapOf(EnhancedType.of(String.class),
                        EnhancedType.of(LocalizedContent.class))))
            .mapConstructor(HashMap::new)
            .keyConverter(StringStringConverter.create())
            .valueConverter(
                MapAttributeConverter.builder(
                        EnhancedType.mapOf(EnhancedType.of(String.class),
                            EnhancedType.of(LocalizedContent.class)))
                    .mapConstructor(HashMap::new)
                    .keyConverter(StringStringConverter.create())
                    .valueConverter(new LocalizedContentAttributeConverter())
                    .build())
            .build();
  }

  @Override
  public AttributeValue transformFrom(LocalizedContentMap map) {
    return mapConverter.transformFrom(map.getContentMap());
  }

  @Override
  public LocalizedContentMap transformTo(AttributeValue attributeValue) {
    LocalizedContentMap map;
    try {
      map = new LocalizedContentMap(mapConverter.transformTo(attributeValue));
    } catch (RuntimeException e) {
      Log.error("Failed to convert attribute value to LocalizedContentMap", e);
      map = new LocalizedContentMap(new HashMap<>());
    }

    return map;
  }

  @Override
  public EnhancedType<LocalizedContentMap> type() {
    return EnhancedType.of(LocalizedContentMap.class);
  }

  @Override
  public AttributeValueType attributeValueType() {
    return mapConverter.attributeValueType();
  }
}