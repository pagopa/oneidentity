package it.pagopa.oneid.common.model.converters;

import it.pagopa.oneid.common.model.Client.LocalizedContent;
import java.util.HashMap;
import java.util.Map;
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public class LocalizedContentAttributeConverter implements AttributeConverter<LocalizedContent> {

  @Override
  public AttributeValue transformFrom(LocalizedContent localizedContent) {
    Map<String, AttributeValue> map = new HashMap<>();
    map.put("title", AttributeValue.builder().s(localizedContent.title()).build());
    map.put("description", AttributeValue.builder().s(localizedContent.description()).build());
    map.put("cookieUri", AttributeValue.builder().s(localizedContent.cookieUri()).build());
    map.put("docUri", AttributeValue.builder().s(localizedContent.docUri()).build());
    map.put("supportAddress",
        AttributeValue.builder().s(localizedContent.supportAddress()).build());
    return AttributeValue.builder().m(map).build();
  }

  @Override
  public LocalizedContent transformTo(AttributeValue attributeValue) {
    Map<String, AttributeValue> map = attributeValue.m();
    return new LocalizedContent(
        map.containsKey("title") ? map.get("title").s() : "",
        map.containsKey("description") ? map.get("description").s() : "",
        map.containsKey("cookieUri") ? map.get("cookieUri").s() : "",
        map.containsKey("docUri") ? map.get("docUri").s() : "",
        map.containsKey("supportAddress") ? map.get("supportAddress").s() : ""
    );
  }

  @Override
  public EnhancedType<LocalizedContent> type() {
    return EnhancedType.of(LocalizedContent.class);
  }

  @Override
  public AttributeValueType attributeValueType() {
    return AttributeValueType.M;
  }
}