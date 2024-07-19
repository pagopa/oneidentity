package it.pagopa.oneid.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
  @JsonProperty("redirect_uris")
  private List<String> redirectUris; //Client.callbackURI
  @NotBlank
  @JsonProperty("client_name")
  private String clientName; //Client.friendlyName
  @JsonProperty("logo_uri")
  private URI logoUri;
  @NotEmpty
  @JsonProperty("default_acr_values")
  private List<AuthLevel> defaultAcrValues;
  @NotEmpty
  @JsonProperty("saml_requested_attributes")
  private List<Identifier> samlRequestedAttributes;


  public ClientMetadataDTO(ClientMetadataDTO clientMetadataDTO) {
    this.redirectUris = clientMetadataDTO.getRedirectUris();
    this.clientName = clientMetadataDTO.getClientName();
    this.logoUri = clientMetadataDTO.getLogoUri();
    this.defaultAcrValues = clientMetadataDTO.getDefaultAcrValues();
    this.samlRequestedAttributes = clientMetadataDTO.getSamlRequestedAttributes();
  }
}
