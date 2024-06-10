package it.pagopa.oneid.web.controller;

import it.pagopa.oneid.web.dto.AccessTokenDTO;
import it.pagopa.oneid.web.dto.SAMLResponseDTO;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.net.URISyntaxException;

@Path(("/saml"))
public class SAMLController {

    @POST
    @Path("/acs")
    @Produces(MediaType.TEXT_HTML)

    public Response samlACS(@BeanParam SAMLResponseDTO samlResponseDTO) {
        // TODO Remove mock and set @Valid param
        if (samlResponseDTO.getSAMLResponse().equals("test")) {
            Response redirectResponse = null;
            try {
                redirectResponse = Response
                        .status(302)
                        .location(new URI("/login"))
                        .build();
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
            return redirectResponse;
        } else return Response.ok("<html lang=\"it\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title>HTML di esempio</title>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <h1>HTML di esempio</h1>\n" +
                "</body>\n" +
                "</html>").build();
    }

    @GET
    @Path("/assertion")
    public Response assertion(@BeanParam @Valid AccessTokenDTO accessToken) {
        return Response.ok("Assertion Path").build();
    }

}
