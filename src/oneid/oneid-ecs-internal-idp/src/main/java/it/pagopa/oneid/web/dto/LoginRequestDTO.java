package it.pagopa.oneid.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.jboss.resteasy.reactive.RestForm;

@Data
public class LoginRequestDTO {

  @RestForm("AuthnRequestId")
  @NotBlank
  private String authnRequestId;

  @RestForm("ClientId")
  @NotBlank
  private String clientId;

  @RestForm("username")
  @NotBlank
  private String username;

  @RestForm("password")
  @NotBlank
  private String password;
}
