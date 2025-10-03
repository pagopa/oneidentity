package it.pagopa.oneid.common.connector;

import it.pagopa.oneid.common.model.dto.PDVUserUpsertResponseDTO;
import it.pagopa.oneid.common.model.dto.SavePDVUserDTO;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "pdv-api")
@Path("/users")
public interface PDVApiClient {

  @PATCH
  @Path("/user-registry/v1/users")
  PDVUserUpsertResponseDTO upsertUser(
      @RequestBody SavePDVUserDTO savePDVUserDTO,
      @HeaderParam("x-api-key") String apiKey
  );

}
