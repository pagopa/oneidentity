package it.pagopa.oneid.common.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class PDVValidateApiKeyDTO {

  @NotNull
  @JsonProperty("api_key_id")
  private String apiKeyId;

  @NotNull
  @JsonProperty("api_key_value")
  private String apiKeyValue;
}
