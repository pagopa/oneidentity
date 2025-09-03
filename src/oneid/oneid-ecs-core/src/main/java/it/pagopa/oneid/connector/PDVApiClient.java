package it.pagopa.oneid.connector;

import it.pagopa.oneid.model.dto.PDVUserUpsertResponseDTO;
import it.pagopa.oneid.model.dto.SavePDVUserDTO;
import jakarta.validation.Valid;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "pdv-api")
@Path("/user-registry/v1/users")
public interface PDVApiClient {

  @PATCH
  PDVUserUpsertResponseDTO upsertUser(
      @BeanParam @Valid SavePDVUserDTO savePDVUserDTO,
      @HeaderParam("X-API-Key") String apiKey
  );
}
