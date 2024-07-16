package it.pagopa.oneid.common.model.enums.converter;

import it.pagopa.oneid.common.model.enums.AuthLevel;
import jakarta.inject.Inject;
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public class AuthLevelConverter implements AttributeConverter<AuthLevel> {

  @Inject
  public AuthLevelConverter() {
  }

  @Override
  public AttributeValue transformFrom(AuthLevel input) {
    return AttributeValue.builder().s(input.getValue()).build();
  }

  @Override
  public AuthLevel transformTo(AttributeValue input) {
    return AuthLevel.authLevelFromValue(input.s());
  }

  @Override
  public EnhancedType<AuthLevel> type() {
    return EnhancedType.of(AuthLevel.class);
  }

  @Override
  public AttributeValueType attributeValueType() {
    return AttributeValueType.S;
  }
}