package it.pagopa.oneid.web.dto;

import it.pagopa.oneid.model.dto.TokenRequestDTO;
import jakarta.ws.rs.HeaderParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class TokenRequestDTOExtended extends TokenRequestDTO {

  @HeaderParam("Authorization")
  private String authorization;

}
