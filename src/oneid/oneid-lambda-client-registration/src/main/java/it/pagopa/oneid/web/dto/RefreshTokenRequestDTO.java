package it.pagopa.oneid.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshTokenRequestDTO {

  @NotBlank
  @JsonProperty("userId")
  private String userId;

}
