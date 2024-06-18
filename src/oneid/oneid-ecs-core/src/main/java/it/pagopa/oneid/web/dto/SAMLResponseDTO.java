package it.pagopa.oneid.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.jboss.resteasy.reactive.RestForm;

@Data
public class SAMLResponseDTO {

  @RestForm
  @NotBlank
  String SAMLResponse;
  @RestForm
  @NotBlank
  String RelayState;
}
