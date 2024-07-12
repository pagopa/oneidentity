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
  private int clientSecretExpiresAt; //TODO set to 0, never expires
}
