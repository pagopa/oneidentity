package it.pagopa.oneid.model.dto;

import it.pagopa.oneid.common.model.enums.AuthLevel;
import it.pagopa.oneid.common.model.enums.Identifier;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.net.URI;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ClientMetadataDTO {

  @NotEmpty
  private List<String> redirectUris; //Client.callbackURI
  @NotBlank
  private String clientName; //Client.friendlyName
  private URI logoUri;
  @NotEmpty
  private List<AuthLevel> defaultAcrValues;
  @NotEmpty
  private List<Identifier> samlRequestedAttributes;


  public ClientMetadataDTO(ClientMetadataDTO clientMetadataDTO) {
    this.redirectUris = clientMetadataDTO.getRedirectUris();
    this.clientName = clientMetadataDTO.getClientName();
    this.logoUri = clientMetadataDTO.getLogoUri();
    this.defaultAcrValues = clientMetadataDTO.getDefaultAcrValues();
    this.samlRequestedAttributes = clientMetadataDTO.getSamlRequestedAttributes();
  }
}
