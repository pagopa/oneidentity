package it.pagopa.oneid.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class TokenDataDTO {

  @JsonProperty("access_token")
  @NotNull
  String accessToken;

  @JsonProperty("token_type")
  @NotNull
  String tokenType;

  @JsonProperty("expires_in")
  @NotNull
  String expiresIn;

  @JsonProperty("refresh_token")
  String refreshToken;

  @JsonProperty("scope")
  @NotNull
  String scope;

  @JsonProperty("id_token")
  @NotNull
  String idToken;

  @JsonProperty("id_token_type")
  @NotNull
  String idTokenType;
}
