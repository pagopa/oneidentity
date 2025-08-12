package it.pagopa.oneid.common.model;

import jakarta.validation.constraints.NotNull;
import java.util.Optional;
import java.util.Set;
import lombok.Data;
import lombok.Getter;

@Getter
@Data
public class ClientFE {

  public String clientID;
  public String friendlyName;
  public String logoUri;
  public String policyUri;
  public String tosUri;
  public String a11yUri;
  public boolean backButtonEnabled;
  public Set<String> callbackURI;
  LocalizedContentMap localizedContentMap;

  public ClientFE(@NotNull Client client) {
    this.clientID = Optional.ofNullable(client.getClientId()).orElse("");
    this.friendlyName = Optional.ofNullable(client.getFriendlyName()).orElse("");
    this.logoUri = Optional.ofNullable(client.getLogoUri()).orElse("");
    this.policyUri = Optional.ofNullable(client.getPolicyUri()).orElse("");
    this.tosUri = Optional.ofNullable(client.getTosUri()).orElse("");
    this.a11yUri = Optional.ofNullable(client.getA11yUri()).orElse("");
    this.backButtonEnabled = Optional.of(client.isBackButtonEnabled()).orElse(false);
    this.localizedContentMap = Optional.ofNullable(client.getLocalizedContentMap())
        .orElse(LocalizedContentMap.builder().build());

    this.callbackURI = Optional.ofNullable(client.getCallbackURI())
        .orElse(Set.of());
  }
}
