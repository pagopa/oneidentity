package it.pagopa.oneid.service;

import it.pagopa.oneid.service.utils.SAMLUtils;
import jakarta.inject.Inject;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.security.SecurityException;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.opensaml.xmlsec.signature.support.Signer;

import java.time.Instant;
import java.util.List;

import static it.pagopa.oneid.service.utils.SAMLUtils.*;

public class SAMLServiceImpl implements SAMLService {

    @Inject
    SAMLUtils samlUtils;

    @Override
    public AuthnRequest buildAuthnRequest(String idpID, int assertionConsumerServiceIndex, int attributeConsumingServiceIndex, String purpose) throws SecurityException, SignatureException {

        // Create AuthnRequest
        AuthnRequest authnRequest = SAMLUtils.buildSAMLObject(AuthnRequest.class);
        authnRequest.setIssueInstant(Instant.now());
        authnRequest.setForceAuthn(true);
        authnRequest.setProtocolBinding(SAMLConstants.SAML2_POST_BINDING_URI);
        authnRequest.setAssertionConsumerServiceURL(System.getenv("ACS_URL"));
        authnRequest.setID(generateSecureRandomId());
        authnRequest.setIssuer(buildIssuer());
        authnRequest.setNameIDPolicy(buildNameIdPolicy());
        authnRequest.setRequestedAuthnContext(buildRequestedAuthnContext());

        authnRequest.setDestination(idpID); // idp url in entityID (same as Location of SingleSignOn)
        authnRequest.setAssertionConsumerServiceIndex(assertionConsumerServiceIndex);
        authnRequest.setAttributeConsumingServiceIndex(attributeConsumingServiceIndex);


        // Create signature
        Signature signature = samlUtils.buildSignature(authnRequest);
        authnRequest.setSignature(signature);
        Signer.signObject(signature);

        return authnRequest;
    }


    @Override
    public Response getSAMLResponseFromString(String SAMLResponse) {
        return null;
    }

    @Override
    public boolean validateSAMLResponse(Response SAMLResponse, String idpID) {
        return false;
    }

    @Override
    public List<Attribute> getAttributesFromSAMLResponse(Response SAMLResponse) {
        return List.of();
    }
}
