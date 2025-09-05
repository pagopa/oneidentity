package it.pagopa.oneid.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class SavePDVUserDTO {

  @NotNull
  String fiscalCode;
}
