package it.pagopa.oneid.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AttributeDTO<T> {

  String attributeName;
  T attributeValue;
}
