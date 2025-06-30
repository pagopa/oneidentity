package it.pagopa.oneid.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.CookieParam;
import lombok.Data;
import org.jboss.resteasy.reactive.RestForm;

@Data
public class LoginRequestDTO {

  @CookieParam("AuthnRequestID")
  @NotBlank
  private String authnRequestId;

  @CookieParam("ClientID")
  @NotBlank
  private String clientId;

  @RestForm("username")
  @NotBlank
  private String username;

  @RestForm("password")
  @NotBlank
  private String password;
}
