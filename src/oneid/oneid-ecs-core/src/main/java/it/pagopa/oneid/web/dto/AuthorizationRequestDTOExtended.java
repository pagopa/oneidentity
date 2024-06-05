package it.pagopa.oneid.web.dto;

import it.pagopa.oneid.model.dto.AuthorizationRequestDTO;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.jboss.resteasy.reactive.RestQuery;

@Data
public class AuthorizationRequestDTOExtended extends AuthorizationRequestDTO {
    @RestQuery
    @NotBlank
    private String idp;
}
