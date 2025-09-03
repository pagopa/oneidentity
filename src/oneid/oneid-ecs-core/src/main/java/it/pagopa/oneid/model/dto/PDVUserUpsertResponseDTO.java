package it.pagopa.oneid.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PDVUserUpsertResponseDTO {

  @JsonProperty("id")
  @NotNull
  String userId;
}
