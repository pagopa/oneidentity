package it.pagopa.oneid.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.CookieParam;
import lombok.Data;

@Data
public class ConsentRequestDTO {

  @CookieParam("AuthnRequestID")
  @NotBlank
  private String authnRequestId;

  @CookieParam("username")
  @NotBlank
  private String username;

  @NotBlank
  private boolean consent;

}
