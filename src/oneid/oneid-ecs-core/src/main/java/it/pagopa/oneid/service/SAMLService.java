package it.pagopa.oneid.service;

import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.security.SecurityException;
import org.opensaml.xmlsec.signature.support.SignatureException;

import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;

public interface SAMLService {

    AuthnRequest buildAuthnRequest(String idpID, int assertionConsumerServiceIndex, int attributeConsumingServiceIndex, String purpose) throws SecurityException, CertificateException, IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, MarshallingException, TransformerException;

    Response getSAMLResponseFromString(String SAMLResponse);

    boolean validateSAMLResponse(Response SAMLResponse, String idpID);

    List<Attribute> getAttributesFromSAMLResponse(Response SAMLResponse);

}
