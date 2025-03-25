package it.pagopa.oneid.common.model.converters;

import it.pagopa.oneid.common.model.Client.LocalizedContent;
import java.util.HashMap;
import java.util.Map;
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.enhanced.dynamodb.internal.converter.attribute.MapAttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.internal.converter.string.StringStringConverter;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public class HashMapAttributeConverter implements
    AttributeConverter<Map<String, LocalizedContent>> {

  private static AttributeConverter<Map<String, LocalizedContent>> mapConverter;

  public HashMapAttributeConverter() {
    mapConverter =
        MapAttributeConverter.builder(EnhancedType.mapOf(String.class, LocalizedContent.class))
            .mapConstructor(HashMap::new)
            .keyConverter(StringStringConverter.create())
            .valueConverter(new LocalizedContentAttributeConverter())
            .build();
  }

  @Override
  public AttributeValue transformFrom(Map<String, LocalizedContent> map) {
    return mapConverter.transformFrom(map);
  }

  @Override
  public Map<String, LocalizedContent> transformTo(AttributeValue attributeValue) {
    return mapConverter.transformTo(attributeValue);
  }

  @Override
  public EnhancedType<Map<String, LocalizedContent>> type() {
    return mapConverter.type();
  }

  @Override
  public AttributeValueType attributeValueType() {
    return mapConverter.attributeValueType();
  }
}