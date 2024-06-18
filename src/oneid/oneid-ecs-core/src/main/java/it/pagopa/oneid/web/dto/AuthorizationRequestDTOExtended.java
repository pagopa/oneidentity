package it.pagopa.oneid.web.dto;

import it.pagopa.oneid.model.dto.AuthorizationRequestDTO;
import it.pagopa.oneid.model.session.enums.ResponseType;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class AuthorizationRequestDTOExtended extends AuthorizationRequestDTO {

  @NotBlank
  private String idp;

  @Builder
  public AuthorizationRequestDTOExtended(String idp, String clientId, ResponseType responseType,
      String redirectUri, String scope, String nonce, String state) {
    super(clientId, responseType, redirectUri, scope, nonce, state);
    this.idp = idp;
  }
}
