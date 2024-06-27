package it.pagopa.oneid.common.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SecretDTO {

  String secret;
  String salt;
}
