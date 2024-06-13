package it.pagopa.oneid.service;

import it.pagopa.oneid.service.utils.SAMLUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.Marshaller;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.security.SecurityException;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.opensaml.xmlsec.signature.support.Signer;
import org.w3c.dom.Element;

import java.time.Instant;
import java.util.List;

import static it.pagopa.oneid.service.utils.SAMLUtils.*;

@ApplicationScoped
public class SAMLServiceImpl implements SAMLService {

    @Inject
    SAMLUtils samlUtils;

    @ConfigProperty(name = "acs_url") //TODO substitute with quarkus annotation
    private static final String ACS_URL = System.getenv("ACS_URL");

    @Override
    public AuthnRequest buildAuthnRequest(String idpID, int assertionConsumerServiceIndex, int attributeConsumingServiceIndex, String purpose, String spidLevel) throws SecurityException, SignatureException, MarshallingException {

        AuthnRequest authnRequest = SAMLUtils.buildSAMLObject(AuthnRequest.class);

        //TODO: add support for CIEid

        // Create AuthnRequest
        authnRequest.setIssueInstant(Instant.now());
        authnRequest.setForceAuthn(true);
        authnRequest.setProtocolBinding(SAMLConstants.SAML2_POST_BINDING_URI);
        authnRequest.setAssertionConsumerServiceURL(ACS_URL);
        authnRequest.setID(generateSecureRandomId());
        authnRequest.setIssuer(buildIssuer());
        authnRequest.setNameIDPolicy(buildNameIdPolicy());
        authnRequest.setRequestedAuthnContext(buildRequestedAuthnContext(spidLevel)); //TODO consider passing here spid/cie level

        authnRequest.setDestination(samlUtils.buildDestination(idpID));
        authnRequest.setAssertionConsumerServiceIndex(assertionConsumerServiceIndex);
        authnRequest.setAttributeConsumingServiceIndex(attributeConsumingServiceIndex);


        Signature signature = samlUtils.buildSignature(authnRequest);
        authnRequest.setSignature(signature);
        Marshaller out = XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(authnRequest);
        Element plaintextElement = out.marshall(authnRequest);
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
