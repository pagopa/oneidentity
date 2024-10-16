package it.pagopa.oneid.web.dto;

import it.pagopa.oneid.model.dto.AuthorizationRequestDTO;
import it.pagopa.oneid.model.session.enums.ResponseType;
import jakarta.validation.constraints.NotBlank;
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
public class AuthorizationRequestDTOExtended extends AuthorizationRequestDTO {

  @NotBlank
  private String idp;
  @NotBlank
  private String ipAddress;

  @Builder
  public AuthorizationRequestDTOExtended(String idp, String clientId, ResponseType responseType,
      String redirectUri, String scope, String nonce, String state, String ipAddress) {
    super(clientId, responseType, redirectUri, scope, nonce, state);
    this.idp = idp;
    this.ipAddress = ipAddress;
  }
}
