package it.pagopa.oneid.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.jboss.resteasy.reactive.RestCookie;
import org.jboss.resteasy.reactive.RestForm;

@Data
public class ConsentRequestDTO {

  @RestForm("AuthnRequestId")
  @NotBlank
  private String authnRequestId;

  @RestForm("ClientId")
  @NotBlank
  private String clientId;

  @RestCookie("username")
  @NotBlank
  private String username;

  @RestForm("consent")
  private boolean consent;

}
