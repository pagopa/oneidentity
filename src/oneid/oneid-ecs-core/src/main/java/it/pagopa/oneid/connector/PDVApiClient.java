package it.pagopa.oneid.connector;

import it.pagopa.oneid.model.dto.PDVUserUpsertResponseDTO;
import it.pagopa.oneid.model.dto.SavePDVUserDTO;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "pdv-api")
@Path("/user-registry/v1/users")
public interface PDVApiClient {

  @PATCH
  PDVUserUpsertResponseDTO upsertUser(
      @RequestBody SavePDVUserDTO savePDVUserDTO,
      @HeaderParam("x-api-key") String apiKey
  );
}
