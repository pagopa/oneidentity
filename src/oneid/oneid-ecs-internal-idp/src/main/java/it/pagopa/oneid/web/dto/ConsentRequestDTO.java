package it.pagopa.oneid.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.jboss.resteasy.reactive.RestCookie;

@Data
public class ConsentRequestDTO {

  @RestCookie("AuthnRequestId")
  @NotBlank
  private String authnRequestId;

  @RestCookie("ClientId")
  @NotBlank
  private String clientId;

  @RestCookie("username")
  @NotBlank
  private String username;
  
  private boolean consent;

}
