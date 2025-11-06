package it.pagopa.oneid.common.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class PDVValidateApiKeyDTO {

  @NotNull
  @JsonProperty("api_key_id")
  private String apiKeyId;

  @NotNull
  @JsonProperty("api_key_value")
  private String apiKeyValue;
}
