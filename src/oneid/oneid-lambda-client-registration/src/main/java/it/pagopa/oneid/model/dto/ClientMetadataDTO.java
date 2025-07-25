package it.pagopa.oneid.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
import it.pagopa.oneid.common.model.Client.LocalizedContent;
import it.pagopa.oneid.common.model.enums.Identifier;
import it.pagopa.oneid.web.validator.annotations.AuthLevelCheck;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.Map;
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
  @JsonProperty("redirectUris")
  @Parameter(explode = Explode.TRUE, style = ParameterStyle.FORM)
  private Set<String> redirectUris; //Client.callbackURI

  @NotBlank
  @JsonProperty("clientName")
  private String clientName; //Client.friendlyName

  @JsonProperty("logoUri")
  private String logoUri;

  @JsonProperty("policyUri")
  private String policyUri;

  @JsonProperty("tosUri")
  private String tosUri;

  @JsonProperty("defaultAcrValues")
  @Parameter(explode = Explode.TRUE, style = ParameterStyle.FORM)
  @AuthLevelCheck
  private Set<String> defaultAcrValues;

  @NotEmpty
  @JsonProperty("samlRequestedAttributes")
  @Parameter(explode = Explode.TRUE, style = ParameterStyle.FORM)
  private Set<Identifier> samlRequestedAttributes;

  @JsonProperty("requiredSameIdp")
  private boolean requiredSameIdp;

  @JsonProperty("a11yUri")
  private String a11yUri;

  @JsonProperty("backButtonEnabled")
  private boolean backButtonEnabled;

  @JsonProperty("localizedContentMap")
  private Map<String, Map<String, LocalizedContent>> localizedContentMap;

  @JsonProperty("spidMinors")
  private boolean spidMinors;

  @JsonProperty("spidProfessionals")
  private boolean spidProfessionals;

  @JsonProperty("pairwise")
  private boolean pairwise;

  public ClientMetadataDTO(ClientMetadataDTO clientMetadataDTO) {
    this.redirectUris = clientMetadataDTO.getRedirectUris();
    this.clientName = clientMetadataDTO.getClientName();
    this.logoUri = clientMetadataDTO.getLogoUri();
    this.policyUri = clientMetadataDTO.getPolicyUri();
    this.tosUri = clientMetadataDTO.getTosUri();
    this.defaultAcrValues = clientMetadataDTO.getDefaultAcrValues();
    this.samlRequestedAttributes = clientMetadataDTO.getSamlRequestedAttributes();
    this.requiredSameIdp = clientMetadataDTO.isRequiredSameIdp();
    this.a11yUri = clientMetadataDTO.getA11yUri();
    this.backButtonEnabled = clientMetadataDTO.isBackButtonEnabled();
    this.localizedContentMap = clientMetadataDTO.getLocalizedContentMap();
    this.spidMinors = clientMetadataDTO.isSpidMinors();
    this.spidProfessionals = clientMetadataDTO.isSpidProfessionals();
    this.pairwise = clientMetadataDTO.isPairwise();
  }
}
