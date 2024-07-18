package it.pagopa.oneid.model.dto;

import com.nimbusds.oauth2.sdk.id.ClientID;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class ClientRegistrationResponseDTO extends ClientMetadataDTO {

  @NotBlank
  private ClientID clientID;
  private String clientSecret;
  private long clientIdIssuedAt;
  private int clientSecretExpiresAt;


  public ClientRegistrationResponseDTO(ClientMetadataDTO clientMetadataDTO, ClientID clientID,
      String clientSecret,
      long clientIdIssuedAt) {
    super(clientMetadataDTO);
    this.clientID = clientID;
    this.clientSecret = clientSecret;
    this.clientIdIssuedAt = clientIdIssuedAt;
    clientSecretExpiresAt = 0;
  }
}
