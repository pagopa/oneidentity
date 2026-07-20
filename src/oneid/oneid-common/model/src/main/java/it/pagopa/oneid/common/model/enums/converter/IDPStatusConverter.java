package it.pagopa.oneid.common.model.enums.converter;

import it.pagopa.oneid.common.model.enums.IDPStatus;
import java.util.Locale;
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

/**
 * Converts persisted IDP statuses to known domain values, using KO for invalid data.
 */
public class IDPStatusConverter implements AttributeConverter<IDPStatus> {

  @Override
  public AttributeValue transformFrom(IDPStatus input) {
    IDPStatus status = input == null ? IDPStatus.KO : input;
    return AttributeValue.builder().s(status.name()).build();
  }

  @Override
  public IDPStatus transformTo(AttributeValue input) {
    if (input == null || input.s() == null || input.s().isBlank()) {
      return IDPStatus.KO;
    }

    try {
      return IDPStatus.valueOf(input.s().trim().toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException exception) {
      return IDPStatus.KO;
    }
  }

  @Override
  public EnhancedType<IDPStatus> type() {
    return EnhancedType.of(IDPStatus.class);
  }

  @Override
  public AttributeValueType attributeValueType() {
    return AttributeValueType.S;
  }
}
