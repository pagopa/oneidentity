package it.pagopa.oneid.common.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;

@Getter
@Data
public class ClientFE {

  public String clientID;
  public String friendlyName;
  public String logoUri;
  public String ppUri;
  public String tosUri;

  public ClientFE(@NotNull Client client) {
    this.clientID = client.getClientId();
    this.friendlyName = client.getFriendlyName();
    this.logoUri = client.getLogoUri();
    this.ppUri = client.getPolicyUri();
    this.tosUri = client.getTosUri();
  }
}
