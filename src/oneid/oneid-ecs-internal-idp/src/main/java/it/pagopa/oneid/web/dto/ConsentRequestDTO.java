package it.pagopa.oneid.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.jboss.resteasy.reactive.RestForm;

@Data
public class ConsentRequestDTO {

  @RestForm("authnRequestId")
  @NotBlank
  private String authnRequestId;

  @RestForm("clientId")
  @NotBlank
  private String clientId;

  @RestForm("username")
  @NotBlank
  private String username;

  @RestForm("consent")
  private boolean consent;

}
