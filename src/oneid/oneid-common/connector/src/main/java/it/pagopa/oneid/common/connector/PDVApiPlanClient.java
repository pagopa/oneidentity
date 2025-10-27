package it.pagopa.oneid.common.connector;

import it.pagopa.oneid.common.model.dto.PDVApiKeysDTO;
import it.pagopa.oneid.common.model.dto.PDVUserUpsertResponseDTO;
import it.pagopa.oneid.common.model.dto.PDVValidateApiKeyDTO;
import it.pagopa.oneid.common.model.dto.PDVValidationResponseDTO;
import it.pagopa.oneid.common.model.dto.SavePDVUserDTO;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "pdv-plan-api")
@Path("/")
public interface PDVApiPlanClient {

    @GET
    @Path("/api-keys")
    PDVApiKeysDTO getPDVPlans(
            @HeaderParam("x-api-key") String apiKey
    );

    @POST
    @Path("/validate-api-key")
    PDVValidationResponseDTO validatePDVApiKey(
            @RequestBody PDVValidateApiKeyDTO validateApiKeyDTO,
            @HeaderParam("x-api-key") String apiKey
    );
}