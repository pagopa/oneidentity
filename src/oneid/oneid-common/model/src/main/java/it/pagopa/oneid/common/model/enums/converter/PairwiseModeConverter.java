package it.pagopa.oneid.common.model.enums.converter;

import it.pagopa.oneid.common.model.enums.PairwiseMode;
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public class PairwiseModeConverter implements AttributeConverter<PairwiseMode> {

  @Override
  public AttributeValue transformFrom(PairwiseMode input) {
    if (input == null) {
      return AttributeValue.builder().nul(true).build();
    }
    return AttributeValue.builder().s(input.name()).build();
  }

  @Override
  public PairwiseMode transformTo(AttributeValue input) {
    if (input == null || Boolean.TRUE.equals(input.nul())) {
      return null;
    }
    if (input.bool() != null) {
      return PairwiseMode.fromLegacy(input.bool());
    }
    if (input.s() != null) {
      return PairwiseMode.fromLegacy(input.s());
    }
    throw new IllegalArgumentException("Unsupported pairwise DynamoDB attribute: " + input);
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
