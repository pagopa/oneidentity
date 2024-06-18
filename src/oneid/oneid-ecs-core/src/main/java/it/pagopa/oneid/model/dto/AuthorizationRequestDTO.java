package it.pagopa.oneid.model.dto;

import it.pagopa.oneid.model.session.enums.ResponseType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jboss.resteasy.reactive.RestQuery;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AuthorizationRequestDTO {

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

