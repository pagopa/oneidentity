package it.pagopa.oneid.service;

import io.quarkus.logging.Log;
import it.pagopa.oneid.common.model.exception.OneIdentityException;
import it.pagopa.oneid.common.model.exception.SAMLUtilsException;
import it.pagopa.oneid.common.utils.SAMLUtils;
import it.pagopa.oneid.common.utils.logging.CustomLogging;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.ByteArrayInputStream;
import java.util.Base64;
import net.shibboleth.utilities.java.support.xml.BasicParserPool;
import net.shibboleth.utilities.java.support.xml.XMLParserException;
import org.opensaml.core.xml.io.MarshallerFactory;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.security.x509.BasicX509Credential;

@ApplicationScoped
@CustomLogging
public class InternalIDPServiceImpl extends SAMLUtils implements InternalIDPService {

  @Inject
  public InternalIDPServiceImpl(BasicParserPool basicParserPool,
      BasicX509Credential basicX509Credential, MarshallerFactory marshallerFactory)
      throws SAMLUtilsException {
    super(basicParserPool, basicX509Credential, marshallerFactory);
  }

  @Override
  public AuthnRequest getAuthnRequestFromString(String authnRequest) throws OneIdentityException {
    byte[] decodedAuthnRequest = decodeBase64(authnRequest);
    return unmarshallAuthnRequest(decodedAuthnRequest);
  }

  private byte[] decodeBase64(String authnRequest) throws OneIdentityException {
    try {
      return Base64.getDecoder().decode(authnRequest);
    } catch (IllegalArgumentException e) {
      Log.error("Base64 decoding error: " + e.getMessage());
      throw new OneIdentityException(e);
    }
  }

  private AuthnRequest unmarshallAuthnRequest(byte[] decodedAuthnRequest)
      throws OneIdentityException {
    try {
      return (AuthnRequest) XMLObjectSupport.unmarshallFromInputStream(basicParserPool,
          new ByteArrayInputStream(decodedAuthnRequest));
    } catch (XMLParserException | UnmarshallingException e) {
      Log.error("Unmarshalling error: " + e.getMessage());
      throw new OneIdentityException(e);
    }
  }


  @Override
  public AuthnRequest validateAuthnRequest(AuthnRequest authnRequest) throws OneIdentityException {

    //todo validate clientRegistration attributes of authnRequest

    //todo validate spid attributes of authnRequest

    return null;
  }

}
