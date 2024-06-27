package it.pagopa.oneid.web.dto;

import com.nimbusds.jose.shaded.gson.annotations.SerializedName;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class TokenDataDTO {

  @SerializedName("access_token")
  @NotNull
  String accessToken;

  @SerializedName("token_type")
  @NotNull
  String tokenType;

  @SerializedName("expires_in")
  @NotNull
  String expiresIn;

  @SerializedName("refresh_token")
  String refreshToken;

  @SerializedName("scope")
  @NotNull
  String scope;

  @SerializedName("id_token")
  @NotNull
  String idToken;

  @SerializedName("id_token_type")
  @NotNull
  String idTokenType;
}
