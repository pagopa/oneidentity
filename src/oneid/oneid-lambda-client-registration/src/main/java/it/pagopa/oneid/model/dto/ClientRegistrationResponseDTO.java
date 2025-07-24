package it.pagopa.oneid.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@RegisterForReflection
public class ClientRegistrationResponseDTO extends ClientMetadataDTO {

  @NotBlank
  @JsonProperty("clientID")
  private String clientID;

  @JsonProperty("clientSecret")
  private String clientSecret;

  @JsonProperty("clientIdIssuedAt")
  private long clientIdIssuedAt;

  @JsonProperty("clientSecretExpiresAt")
  private int clientSecretExpiresAt;


  public ClientRegistrationResponseDTO(ClientMetadataDTO clientMetadataDTO, String clientID,
      String clientSecret,
      long clientIdIssuedAt) {
    super(clientMetadataDTO);
    this.clientID = clientID;
    this.clientSecret = clientSecret;
    this.clientIdIssuedAt = clientIdIssuedAt;
    clientSecretExpiresAt = 0;
  }
}
