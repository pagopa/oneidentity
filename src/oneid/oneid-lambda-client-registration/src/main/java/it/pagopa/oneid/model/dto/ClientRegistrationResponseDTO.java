package it.pagopa.oneid.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class ClientRegistrationResponseDTO extends ClientMetadataDTO {

  @NotBlank
  @JsonProperty("client_id")
  private String clientID;
  @JsonProperty("client_secret")
  private String clientSecret;
  @JsonProperty("client_id_issued_at")
  private long clientIdIssuedAt;
  @JsonProperty("client_secret_expires_at")
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
