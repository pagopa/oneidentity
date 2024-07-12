package it.pagopa.oneid.model.dto;

import it.pagopa.oneid.common.model.enums.AuthLevel;
import it.pagopa.oneid.common.model.enums.Identifier;
import jakarta.validation.constraints.NotEmpty;
import java.net.URI;
import java.util.List;

public class ClientMetadataDTO {

  @NotEmpty
  private List<String> redirectUris; //Client.callbackURI
  private String clientName; //Client.friendlyName
  private URI logoUri; //todo is it useful?
  @NotEmpty
  private List<AuthLevel> defaultAcrValues;
  @NotEmpty
  private List<Identifier> samlRequestedAttributes;
  
}
