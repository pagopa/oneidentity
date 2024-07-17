package it.pagopa.oneid.web.dto;

import it.pagopa.oneid.model.session.enums.ResponseType;
import it.pagopa.oneid.web.validator.annotations.AuthenticationRequestCheck;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.jboss.resteasy.reactive.RestForm;

@Data
@AuthenticationRequestCheck
public class AuthorizationRequestDTOExtendedPost {

  @NotBlank
  @RestForm
  private String idp;

  @NotBlank
  @Size(max = 255)
  @RestForm("client_id")
  private String clientId;

  @NotNull
  @RestForm("response_type")
  private ResponseType responseType;

  @RestForm("redirect_uri")
  private String redirectUri;

  @RestForm
  private String scope;

  @RestForm
  private String nonce;

  @RestForm
  private String state;

}
