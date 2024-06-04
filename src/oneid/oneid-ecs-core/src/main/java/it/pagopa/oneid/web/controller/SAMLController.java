package it.pagopa.oneid.web.controller;

import it.pagopa.oneid.web.dto.SAMLResponseDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestQuery;

@Path(("/saml"))
public class SAMLController {

    @POST
    @Path("/acs")
    public Response samlACS(@BeanParam @Valid SAMLResponseDTO samlResponseDTO) {
        return Response.ok("ACS Path").build();
    }

    @GET
    @Path("/assertion")
    public Response assertion(@RestQuery("access_token") @NotBlank String accessToken) {
        return Response.ok("Assertion Path").build();
    }
}
