package it.pagopa.oneid.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import it.pagopa.oneid.common.model.enums.Identifier;
import it.pagopa.oneid.web.validator.annotations.AuthLevelCheck;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.enums.Explode;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterStyle;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.jboss.resteasy.reactive.RestForm;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientMetadataDTO {

  @NotEmpty
  @JsonProperty("redirect_uris")
  @RestForm("redirect_uris")
  @Parameter(explode = Explode.TRUE, style = ParameterStyle.FORM)
  private Set<String> redirectUris; //Client.callbackURI

  @NotBlank
  @JsonProperty("client_name")
  @RestForm("client_name")
  private String clientName; //Client.friendlyName

  @JsonProperty("logo_uri")
  @RestForm("logo_uri")
  private String logoUri;

  @JsonProperty("default_acr_values")
  @RestForm("default_acr_values")
  @Parameter(explode = Explode.TRUE, style = ParameterStyle.FORM)
  @AuthLevelCheck
  private Set<String> defaultAcrValues;

  @NotEmpty
  @JsonProperty("saml_requested_attributes")
  @RestForm("saml_requested_attributes")
  @Parameter(explode = Explode.TRUE, style = ParameterStyle.FORM)
  private Set<Identifier> samlRequestedAttributes;


  public ClientMetadataDTO(ClientMetadataDTO clientMetadataDTO) {
    this.redirectUris = clientMetadataDTO.getRedirectUris();
    this.clientName = clientMetadataDTO.getClientName();
    this.logoUri = clientMetadataDTO.getLogoUri();
    this.defaultAcrValues = clientMetadataDTO.getDefaultAcrValues();
    this.samlRequestedAttributes = clientMetadataDTO.getSamlRequestedAttributes();
  }
}
