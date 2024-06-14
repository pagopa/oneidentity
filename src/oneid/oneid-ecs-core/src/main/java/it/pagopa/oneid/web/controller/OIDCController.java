package it.pagopa.oneid.web.controller;

import it.pagopa.oneid.common.Client;
import it.pagopa.oneid.exception.*;
import it.pagopa.oneid.model.dto.AuthorizationRequestDTO;
import it.pagopa.oneid.model.session.SAMLSession;
import it.pagopa.oneid.model.session.enums.RecordType;
import it.pagopa.oneid.web.dto.TokenRequestDTOExtended;
import it.pagopa.oneid.web.utils.WebUtils;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.opensaml.saml.saml2.core.AuthnRequest;
import it.pagopa.oneid.exception.GenericAuthnRequestCreationException;
import it.pagopa.oneid.exception.IDPSSOEndpointNotFoundException;
import it.pagopa.oneid.service.SAMLServiceImpl;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

@Path(("/oidc"))
public class OIDCController<T> {

    @Inject
    SAMLServiceImpl samlServiceImpl;

    @Inject
    SessionServiceImpl<T> sessionServiceImpl;

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
    public Response authorize(@BeanParam @Valid AuthorizationRequestDTO authorizationRequestDTO) throws IDPSSOEndpointNotFoundException, GenericAuthnRequestCreationException {
        // TODO setup logging utility


        // TODO these hashmaps must be initialized during startup
        // HashMap<String, IDP> idpsMap = new HashMap<String, String>(); -> from file
        // HashMap<String, Client> clientsHashMap = new HashMap<String, Client>(); -> from DynamoDB

        // TODO may these controls be executed by other components?

        // 1. Check if idp exists leveraging on runtime map (CIE/SPID) {key: idp, value: idp sso endpoint}
        if (!idpsMap.containsKey(authorizationRequestDTOExtended.getIdp())) {
            throw new IDPNotFoundException();
        }

        // 2. Check if clientId exists leverage on runtime map {key: clientid, value: ClientRegistration}
        if (!clientsHashMap.containsKey(authorizationRequestDTOExtended.getClientId())) {
            throw new ClientNotFoundException();
        }

        // 3. Check if callbackUri exists among clientId parameters
        if (!clientsHashMap.get(authorizationRequestDTOExtended.getClientId()).getCallbackURI().contains(authorizationRequestDTOExtended.getRedirectUri())) {
            throw new CallbackURINotFoundException();
        }

        Client client = clientsHashMap.get(authorizationRequestDTOExtended.getClientId());
        String idpSSOEndpoint = idpsMap.get(authorizationRequestDTOExtended.getIdp()).getSSOEndpoint();

        // 4. Create SAML Authn Request using SAMLServiceImpl
        AuthnRequest authnRequest = samlServiceImpl.buildAuthnRequest(authorizationRequestDTOExtended.getIdp(), client.getAcsIndex(), client.getAttributeIndex());

        String encodedAuthnRequest = Base64.getEncoder().encodeToString(WebUtils.getStringValue(WebUtils.getElementValueFromAuthnRequest(authnRequest)).getBytes());
        String encodedRelayStateString = Base64.getEncoder().encodeToString(WebUtils.getRelayState(authorizationRequestDTOExtended).getBytes());

        // 5. Persist SAMLSession

        // Get the current time in epoch second format
        long creationTime = Instant.now().getEpochSecond();

        // Calculate the expiration time (2 days from now) in epoch second format
        long ttl = Instant.now().plus(2, ChronoUnit.DAYS).getEpochSecond();

        SAMLSession samlSession = new SAMLSession(authnRequest.getID(), RecordType.SAML, creationTime, ttl, encodedAuthnRequest);
        sessionServiceImpl.saveSession(samlSession);

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
