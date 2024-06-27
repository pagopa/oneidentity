package it.pagopa.oneid.common.model;

import it.pagopa.oneid.common.model.dto.SecretDTO;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
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

  @Builder
  public ClientExtended(@NotNull String clientId, @NotNull String friendlyName,
      @NotNull List<String> callbackURI,
      @NotNull List<String> requestedParameters, @NotNull String authLevel,
      @NotNull int acsIndex, @NotNull int attributeIndex, @NotNull boolean isActive,
      String secret, String salt) {
    super(clientId, friendlyName, callbackURI, requestedParameters, authLevel, acsIndex,
        attributeIndex, isActive);
    this.secret = secret;
    this.salt = salt;
  }

  public SecretDTO clientSecretDTO() {
    return new SecretDTO(this.secret, this.salt);
  }

}
