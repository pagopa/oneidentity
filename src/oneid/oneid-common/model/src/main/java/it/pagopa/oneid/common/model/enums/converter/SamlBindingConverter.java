package it.pagopa.oneid.common.model.enums.converter;

import it.pagopa.oneid.common.model.enums.SamlBinding;
import jakarta.inject.Inject;
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public class SamlBindingConverter implements AttributeConverter<SamlBinding> {

  @Inject
  public SamlBindingConverter() {
  }

  @Override
  public AttributeValue transformFrom(SamlBinding input) {
    SamlBinding bindingType = input == null ? SamlBinding.HTTP_POST : input;
    return AttributeValue.builder().s(bindingType.getValue()).build();
  }

  @Override
  public SamlBinding transformTo(AttributeValue input) {
    if (input == null || input.s() == null) {
      return SamlBinding.HTTP_POST;
    }
    return SamlBinding.samlBindingTypeFromValue(input.s());
  }

  @Override
  public EnhancedType<SamlBinding> type() {
    return EnhancedType.of(SamlBinding.class);
  }

  @Override
  public AttributeValueType attributeValueType() {
    return AttributeValueType.S;
  }
}
