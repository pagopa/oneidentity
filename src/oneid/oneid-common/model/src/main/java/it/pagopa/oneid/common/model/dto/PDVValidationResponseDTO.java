package it.pagopa.oneid.common.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@RegisterForReflection
@SuperBuilder
public class PDVValidationResponseDTO {

  @JsonProperty("valid")
  private boolean valid;
}
