package it.pagopa.oneid.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@RegisterForReflection
@SuperBuilder
public class ClientRegistrationResponseDTO extends ClientRegistrationDTO {

  @NotBlank
  @JsonProperty("clientID")
  private String clientID;

  @JsonProperty("clientSecret")
  private String clientSecret;

  @JsonProperty("clientIdIssuedAt")
  private long clientIdIssuedAt;

  @JsonProperty("clientSecretExpiresAt")
  private int clientSecretExpiresAt;
  
}
