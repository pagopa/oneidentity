package it.pagopa.oneid.web.dto;

import it.pagopa.oneid.model.GrantType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.jboss.resteasy.reactive.RestForm;

@Data
public class TokenRequestDTO {

    @RestForm("grant_type")
    @NotNull
    private GrantType grantType;

    @RestForm
    @NotBlank
    private String code;

    @RestForm("redirect_uri")
    @NotBlank
    private String redirectUri;

}

