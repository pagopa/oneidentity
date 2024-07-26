package it.pagopa.oneid.common.model;

import it.pagopa.oneid.common.model.dto.SecretDTO;
import it.pagopa.oneid.common.model.enums.AuthLevel;
import jakarta.validation.constraints.NotNull;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Data
@DynamoDbBean
@EqualsAndHashCode(callSuper = true)
public class ClientExtended extends Client {

  @NotNull
  private String secret;

  @NotNull
  private String salt;

  public ClientExtended(@NotNull String clientId, @NotNull String friendlyName,
      @NotNull Set<String> callbackURI,
      @NotNull Set<String> requestedParameters, @NotNull AuthLevel authLevel,
      @NotNull int acsIndex, @NotNull int attributeIndex, @NotNull boolean isActive,
      String secret, String salt, long clientIdIssuedAt, String logoUri) {
    super(clientId, friendlyName, callbackURI, requestedParameters, authLevel, acsIndex,
        attributeIndex, isActive, clientIdIssuedAt, logoUri);
    this.secret = secret;
    this.salt = salt;
  }

  public ClientExtended(Client client, String secret, String salt) {
    super(client.getClientId(), client.getFriendlyName(), client.getCallbackURI(),
        client.getRequestedParameters(), client.getAuthLevel(), client.getAcsIndex(),
        client.getAttributeIndex(), client.isActive(),
        client.getClientIdIssuedAt(), client.getLogoUri());
    this.secret = secret;
    this.salt = salt;
  }

  public SecretDTO clientSecretDTO() {
    return new SecretDTO(this.secret, this.salt);
  }

}
