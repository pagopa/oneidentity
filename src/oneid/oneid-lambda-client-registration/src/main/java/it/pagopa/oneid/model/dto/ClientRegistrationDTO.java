package it.pagopa.oneid.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
import it.pagopa.oneid.common.model.Client.LocalizedContent;
import it.pagopa.oneid.model.groups.ValidationGroups.Registration;
import it.pagopa.oneid.model.groups.ValidationGroups.UpdateClient;
import it.pagopa.oneid.web.validator.annotations.AuthLevelCheck;
import it.pagopa.oneid.web.validator.annotations.LocalizedContentMapCheck;
import it.pagopa.oneid.web.validator.annotations.SamlRequestedAttributeCheck;
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
public class ClientRegistrationDTO {

  @NotBlank(groups = {Registration.class, UpdateClient.class})
  @JsonProperty("userId")
  @Parameter()
  private String userId;

  @NotEmpty(groups = {Registration.class, UpdateClient.class})
  @JsonProperty("redirectUris")
  @Parameter(explode = Explode.TRUE, style = ParameterStyle.FORM)
  private Set<String> redirectUris; //Client.callbackURI

  @NotBlank(groups = {Registration.class, UpdateClient.class})
  @JsonProperty("clientName")
  private String clientName; //Client.friendlyName

  @NotEmpty(groups = {Registration.class, UpdateClient.class})
  @JsonProperty("defaultAcrValues")
  @Parameter(explode = Explode.TRUE, style = ParameterStyle.FORM)
  @AuthLevelCheck(groups = {Registration.class, UpdateClient.class})
  private Set<String> defaultAcrValues;

  @NotEmpty(groups = {Registration.class, UpdateClient.class})
  @JsonProperty("samlRequestedAttributes")
  @Parameter(explode = Explode.TRUE, style = ParameterStyle.FORM)
  @SamlRequestedAttributeCheck(groups = {Registration.class, UpdateClient.class})
  private Set<String> samlRequestedAttributes;

  @JsonProperty("requiredSameIdp")
  private Boolean requiredSameIdp;

  @JsonProperty("logoUri")
  private String logoUri;

  @JsonProperty("policyUri")
  private String policyUri;

  @JsonProperty("tosUri")
  private String tosUri;

  @JsonProperty("a11yUri")
  private String a11yUri;

  @JsonProperty("backButtonEnabled")
  private Boolean backButtonEnabled;

  @JsonProperty("localizedContentMap")
  @LocalizedContentMapCheck(groups = {Registration.class, UpdateClient.class})
  private Map<String, Map<String, LocalizedContent>> localizedContentMap;

  @JsonProperty("spidMinors")
  private Boolean spidMinors;

  @JsonProperty("spidProfessionals")
  private Boolean spidProfessionals;

  @JsonProperty("pairwise")
  private Boolean pairwise;

  public ClientRegistrationDTO(ClientRegistrationDTO clientRegistrationDTO) {
    this.userId = clientRegistrationDTO.userId;
    this.redirectUris = clientRegistrationDTO.redirectUris;
    this.clientName = clientRegistrationDTO.clientName;
    this.defaultAcrValues = clientRegistrationDTO.defaultAcrValues;
    this.samlRequestedAttributes = clientRegistrationDTO.samlRequestedAttributes;
    this.requiredSameIdp = clientRegistrationDTO.requiredSameIdp;
    this.logoUri = clientRegistrationDTO.logoUri;
    this.policyUri = clientRegistrationDTO.policyUri;
    this.tosUri = clientRegistrationDTO.tosUri;
    this.a11yUri = clientRegistrationDTO.a11yUri;
    this.backButtonEnabled = clientRegistrationDTO.backButtonEnabled;
    this.localizedContentMap = clientRegistrationDTO.localizedContentMap;
    this.spidMinors = clientRegistrationDTO.spidMinors;
    this.spidProfessionals = clientRegistrationDTO.spidProfessionals;
    this.pairwise = clientRegistrationDTO.pairwise;
  }
}
