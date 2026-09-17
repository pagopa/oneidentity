package it.pagopa.oneid.common.model.enums.converter;

import it.pagopa.oneid.common.model.enums.PairwiseMode;
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public class PairwiseModeConverter implements AttributeConverter<PairwiseMode> {

  @Override
  public AttributeValue transformFrom(PairwiseMode input) {
    return AttributeValue.builder().s(input.name()).build();
  }

  @Override
  public PairwiseMode transformTo(AttributeValue input) {
    return PairwiseMode.valueOf(input.s());
  }

  @Override
  public EnhancedType<PairwiseMode> type() {
    return EnhancedType.of(PairwiseMode.class);
  }

  @Override
  public AttributeValueType attributeValueType() {
    return AttributeValueType.S;
  }
}
