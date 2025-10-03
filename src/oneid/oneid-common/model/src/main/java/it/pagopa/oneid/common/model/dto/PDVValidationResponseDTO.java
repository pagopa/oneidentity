package it.pagopa.oneid.common.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterForReflection
@SuperBuilder
public class PDVValidationResponseDTO {

  @JsonProperty("valid")
  private boolean valid;
}
