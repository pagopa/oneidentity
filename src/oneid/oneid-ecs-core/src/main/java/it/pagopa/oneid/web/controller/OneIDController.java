package it.pagopa.oneid.web.controller;

import it.pagopa.oneid.model.dto.AuthorizationRequestDTO;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;


@Path("/")
public class OneIDController {

    // TODO replace with quarkus default health check
    @GET
    @Path("/ping")
    public Response ping() {
        return Response.ok("Ping").build();
    }

    @GET
    @Path("/login")
    @Produces(MediaType.TEXT_HTML)
    public Response login(@BeanParam AuthorizationRequestDTO authorizationRequestDTO) {
        // TODO remove this Mock and set @Valid
        String localIDP = "https://localhost:8443/samlsso";
        String localDemoIDP = "https://localhost:8443/demo/samlsso";
        return Response.ok("    <script src=\"https://italia.github.io/spid-smart-button/spid-button.min.js\"></script>\n" +
                "    <div id=\"spid-button\">\n" +
                "      <noscript>\n" +
                "          Il login tramite SPID richiede che JavaScript sia abilitato nel browser.\n" +
                "      </noscript>\n" +
                "    </div>\n" +
                "    <script>\n" +
                "      SPID.init({\n" +
                "        url: '/authorize?idp={{idp}}',\n" +
                "        supported: [\n" +
                "            'https://demo.spid.gov.it/',\n" +
                "        ],\n" +
                "        extraProviders: [{\n" +
                "            \"protocols\": [\"SAML\"],\n" +
                "            \"entityName\": \"IDP test\",\n" +
                "            \"logo\": \"spid-idp-aruba.svg\",\n" +
                "            \"entityID\": \"" + localIDP + "\",\n" +
                "            \"active\": true\n" +
                "        },{\n" +
                "          \"protocols\": [\"SAML\"],\n" +
                "          \"entityName\": \"IDP demo\",\n" +
                "          \"logo\": \"spid-idp-aruba.svg\",\n" +
                "          \"entityID\": \"https://demo.spid.gov.it/\",\n" +
                "          \"active\": true\n" +
                "        },{\n" +
                "          \"protocols\": [\"SAML\"],\n" +
                "          \"entityName\": \"IDP local demo\",\n" +
                "          \"logo\": \"spid-idp-aruba.svg\",\n" +
                "            \"entityID\": \"" + localDemoIDP + "\",\n" +
                "          \"active\": true\n" +
                "        }," +
                "]\n" +
                "      });\n" +
                "    </script>").build();
    }
}
