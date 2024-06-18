package it.pagopa.oneid.web.dto;

import it.pagopa.oneid.model.session.enums.ResponseType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.jboss.resteasy.reactive.RestQuery;

@Data
public class AuthorizationRequestDTOExtendedGet {

  @NotBlank
  @RestQuery
  private String idp;

  @NotBlank
  @Size(max = 255)
  @RestQuery("client_id")
  private String clientId;

  @NotNull
  @RestQuery("response_type")
  private ResponseType responseType;

  @RestQuery("redirect_uri")
  private String redirectUri;

  @RestQuery
  private String scope;

  @RestQuery
  private String nonce;

  @RestQuery
  private String state;

}
