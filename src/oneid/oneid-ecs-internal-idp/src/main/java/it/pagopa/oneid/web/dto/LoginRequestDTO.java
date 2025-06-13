package it.pagopa.oneid.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.CookieParam;
import lombok.Data;

@Data
public class LoginRequestDTO {

  @CookieParam("AuthnRequestID")
  @NotBlank
  private String authnRequestId;

  @JsonProperty("username")
  @NotBlank
  private String username;

  @JsonProperty("password")
  @NotBlank
  private String password;
}
