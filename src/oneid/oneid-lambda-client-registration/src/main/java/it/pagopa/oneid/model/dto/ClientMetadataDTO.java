package it.pagopa.oneid.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
import it.pagopa.oneid.common.model.enums.Identifier;
import it.pagopa.oneid.web.validator.annotations.AuthLevelCheck;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.eclipse.microprofile.openapi.annotations.enums.Explode;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterStyle;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterForReflection
@SuperBuilder
public class ClientMetadataDTO {

  @NotEmpty
  @JsonProperty("redirect_uris")
  @Parameter(explode = Explode.TRUE, style = ParameterStyle.FORM)
  private Set<String> redirectUris; //Client.callbackURI

  @NotBlank
  @JsonProperty("client_name")
  private String clientName; //Client.friendlyName

  @JsonProperty("logo_uri")
  private String logoUri;

  @JsonProperty("policy_uri")
  private String policyUri;

  @JsonProperty("tos_uri")
  private String tosUri;

  @JsonProperty("default_acr_values")
  @Parameter(explode = Explode.TRUE, style = ParameterStyle.FORM)
  @AuthLevelCheck
  private Set<String> defaultAcrValues;

  @NotEmpty
  @JsonProperty("saml_requested_attributes")
  @Parameter(explode = Explode.TRUE, style = ParameterStyle.FORM)
  private Set<Identifier> samlRequestedAttributes;

  @JsonProperty("required_same_idp")
  private boolean requiredSameIdp;


  public ClientMetadataDTO(ClientMetadataDTO clientMetadataDTO) {
    this.redirectUris = clientMetadataDTO.getRedirectUris();
    this.clientName = clientMetadataDTO.getClientName();
    this.logoUri = clientMetadataDTO.getLogoUri();
    this.policyUri = clientMetadataDTO.getPolicyUri();
    this.tosUri = clientMetadataDTO.getTosUri();
    this.defaultAcrValues = clientMetadataDTO.getDefaultAcrValues();
    this.samlRequestedAttributes = clientMetadataDTO.getSamlRequestedAttributes();
    this.requiredSameIdp = clientMetadataDTO.isRequiredSameIdp();
  }
}
