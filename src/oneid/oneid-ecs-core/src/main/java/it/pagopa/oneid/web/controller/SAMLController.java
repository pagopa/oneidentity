package it.pagopa.oneid.web.controller;

import it.pagopa.oneid.web.dto.AccessTokenDTO;
import it.pagopa.oneid.web.dto.SAMLResponseDTO;
import jakarta.validation.Valid;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
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
    if ((samlResponseDTO.getSAMLResponse() != null) && (samlResponseDTO.getSAMLResponse()
        .equals("test"))) {
      try {
        return Response
            .status(302)
            .location(new URI("/login"))
            .build();
      } catch (URISyntaxException e) {
        // throw new RuntimeException(e);
        return Response.status(400).build();
      }
    } else {
      try {
        return Response.status(302).location(new URI("/static/sample_index.html")).build();
      } catch (URISyntaxException e) {
        // throw new RuntimeException(e);
        return Response.status(400).build();
      }
    }
  }

  @GET
  @Path("/assertion")
  public Response assertion(@BeanParam @Valid AccessTokenDTO accessToken) {
    return Response.ok("Assertion Path").build();
  }

}
