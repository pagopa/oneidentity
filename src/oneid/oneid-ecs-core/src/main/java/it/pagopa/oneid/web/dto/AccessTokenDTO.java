package it.pagopa.oneid.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.jboss.resteasy.reactive.RestQuery;

@Data
public class AccessTokenDTO {

  @RestQuery("access_token")
  @NotBlank
  private String accessToken;
}
