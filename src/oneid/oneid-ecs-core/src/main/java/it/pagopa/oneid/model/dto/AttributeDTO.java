package it.pagopa.oneid.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class AttributeDTO {

  String attributeName;
  String attributeValue;
}
