package it.pagopa.oneid.web.controller;

import it.pagopa.oneid.common.Client;
import it.pagopa.oneid.exception.CallbackURINotFoundException;
import it.pagopa.oneid.exception.ClientNotFoundException;
import it.pagopa.oneid.exception.IDPNotFoundException;
import it.pagopa.oneid.service.SAMLServiceImpl;
import it.pagopa.oneid.web.dto.AuthorizationRequestDTOExtended;
import it.pagopa.oneid.web.dto.TokenRequestDTOExtended;
import it.pagopa.oneid.web.utils.WebUtils;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.core.AuthnRequest;
import it.pagopa.oneid.exception.GenericAuthnRequestCreationException;
import it.pagopa.oneid.exception.IDPSSOEndpointNotFoundException;
import it.pagopa.oneid.service.SAMLServiceImpl;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@Path(("/oidc"))
public class OIDCController {

    @Inject
    SAMLServiceImpl samlServiceImpl;

    // TODO remove comment when layer ready
    /*@Inject
    SessionServiceImpl<SAMLSession> samlSessionService;*/

    @Inject
    Map<String, Client> clientsMap;

    @GET
    @Path("/.well-known/openid-configuration")
    @Produces(MediaType.APPLICATION_JSON)
    public Response openIDConfig() {
        return Response.ok("OIDC Configuration Path").build();
    }

    @GET
    @Path("/keys")
    @Produces(MediaType.APPLICATION_JSON)
    public Response keys() {
        return Response.ok("OIDC Keys Path").build();
    }

    @POST
    @Path("/authorize")
    @Produces(MediaType.TEXT_HTML)
    public Response authorize(@BeanParam @Valid AuthorizationRequestDTOExtended authorizationRequestDTOExtended) throws IDPSSOEndpointNotFoundException, GenericAuthnRequestCreationException {
        // TODO setup logging utility

        // TODO setup ExceptionHandler

        // 1. Check if idp exists
        if (samlServiceImpl.getEntityDescriptorFromEntityID(authorizationRequestDTOExtended.getIdp()).isEmpty()) {
            throw new IDPNotFoundException();
        }

        // 2. Check if clientId exists
        if (!clientsMap.containsKey(authorizationRequestDTOExtended.getClientId())) {
            throw new ClientNotFoundException();
        }

        // 3. Check if callbackUri exists among clientId parameters
        if (!clientsMap.get(authorizationRequestDTOExtended.getClientId()).getCallbackURI().contains(authorizationRequestDTOExtended.getRedirectUri())) {
            throw new CallbackURINotFoundException();
        }

        EntityDescriptor idp = samlServiceImpl.getEntityDescriptorFromEntityID(authorizationRequestDTOExtended.getIdp()).get();
        Client client = clientsMap.get(authorizationRequestDTOExtended.getClientId());

        // TODO is it correct to retrieve the 0 indexed?
        String idpSSOEndpoint = idp.getIDPSSODescriptor("urn:oasis:names:tc:SAML:2.0:protocol").getSingleSignOnServices().getFirst().getLocation();

        // 4. Create SAML Authn Request using SAMLServiceImpl
        // TODO implement and replace when layer ready

        String encodedAuthnRequest = "test";
        String encodedRelayStateString = "test";

        /*AuthnRequest authnRequest = samlServiceImpl.buildAuthnRequest(authorizationRequestDTOExtended.getIdp(), client.getAcsIndex(), client.getAttributeIndex());

        String encodedAuthnRequest = Base64.getEncoder().encodeToString(WebUtils.getStringValue(WebUtils.getElementValueFromAuthnRequest(authnRequest)).getBytes());
        String encodedRelayStateString = Base64.getEncoder().encodeToString(WebUtils.getRelayState(authorizationRequestDTOExtended).getBytes());
        */

        // TODO  implement when layer ready

        // 5. Persist SAMLSession

        // Get the current time in epoch second format
        long creationTime = Instant.now().getEpochSecond();

        // Calculate the expiration time (2 days from now) in epoch second format
        long ttl = Instant.now().plus(2, ChronoUnit.DAYS).getEpochSecond();

        //SAMLSession samlSession = new SAMLSession(authnRequest.getID(), RecordType.SAML, creationTime, ttl, encodedAuthnRequest);
        //samlSessionService.saveSession(samlSession);

        String redirectAutoSubmitPOSTForm = "<form method='post' action=" + idpSSOEndpoint
                + " id='SAMLRequestForm'>" +
                "<input type='hidden' name='SAMLRequest' value=" + encodedAuthnRequest + " />" +
                "<input type='hidden' name='RelayState' value=" + encodedRelayStateString + " />" +
                "<input id='SAMLSubmitButton' type='submit' value='Submit' />" +
                "</form>" +
                "<script>document.getElementById('SAMLSubmitButton').style.visibility='hidden'; " +
                "document.getElementById('SAMLRequestForm').submit();</script>";

        return Response
                .ok(redirectAutoSubmitPOSTForm)
                .build();
    }

    @POST
    @Path("/token")
    @Produces(MediaType.APPLICATION_JSON)

    public Response token(@BeanParam @Valid TokenRequestDTOExtended tokenRequestDTOExtended) {
        return Response.ok("Token Path").build();
    }

}
