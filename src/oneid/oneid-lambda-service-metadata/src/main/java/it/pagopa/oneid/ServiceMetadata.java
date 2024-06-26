package it.pagopa.oneid;

import static it.pagopa.oneid.SAMLUtilsExtendedMetadata.buildAssertionConsumerService;
import static it.pagopa.oneid.SAMLUtilsExtendedMetadata.buildAttributeConsumingService;
import static it.pagopa.oneid.SAMLUtilsExtendedMetadata.buildContactPerson;
import static it.pagopa.oneid.SAMLUtilsExtendedMetadata.buildKeyDescriptor;
import static it.pagopa.oneid.SAMLUtilsExtendedMetadata.buildNameIDFormat;
import static it.pagopa.oneid.SAMLUtilsExtendedMetadata.buildOrganization;
import static it.pagopa.oneid.SAMLUtilsExtendedMetadata.buildSPSSODescriptor;
import static it.pagopa.oneid.SAMLUtilsExtendedMetadata.buildSingleLogoutService;
import static it.pagopa.oneid.common.utils.SAMLUtils.buildSignature;
import static it.pagopa.oneid.common.utils.SAMLUtilsConstants.NAMESPACE_PREFIX;
import static it.pagopa.oneid.common.utils.SAMLUtilsConstants.NAMESPACE_URI;
import static it.pagopa.oneid.common.utils.SAMLUtilsConstants.SERVICE_PROVIDER_URI;
import it.pagopa.oneid.common.model.Client;
import it.pagopa.oneid.common.model.exception.OneIdentityException;
import it.pagopa.oneid.common.model.exception.SAMLUtilsException;
import it.pagopa.oneid.common.utils.SAMLUtils;
import it.pagopa.oneid.common.utils.SAMLUtilsConstants;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.StringWriter;
import java.util.Map;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.opensaml.core.xml.Namespace;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.Marshaller;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.opensaml.xmlsec.signature.support.Signer;
import org.w3c.dom.Element;

@Path("/saml")
public class ServiceMetadata {

  @Inject
  Map<String, Client> clientsMap;

  @Inject
  SAMLUtilsExtendedMetadata samlUtils;

  @Inject
  SAMLUtilsConstants samlUtilsConstants;

  public static String getStringValue(Element element) throws SAMLUtilsException {
    StreamResult result = new StreamResult(new StringWriter());
    try {
      TransformerFactory
          .newInstance()
          .newTransformer()
          .transform(new DOMSource(element), result);
    } catch (TransformerException e) {
      throw new SAMLUtilsException(e);
    }
    return result.getWriter().toString();
  }

  @GET
  @Path("/metadata")
  @Produces(MediaType.APPLICATION_XML)
  public Response metadata() throws OneIdentityException {

    EntityDescriptor entityDescriptor = SAMLUtils.buildSAMLObject(EntityDescriptor.class);
    entityDescriptor.setEntityID(SERVICE_PROVIDER_URI);

    SPSSODescriptor spssoDescriptor = buildSPSSODescriptor();
    spssoDescriptor.getKeyDescriptors().add(buildKeyDescriptor());
    spssoDescriptor.getNameIDFormats().add(buildNameIDFormat());
    spssoDescriptor.getAssertionConsumerServices().add(buildAssertionConsumerService());
    spssoDescriptor.getSingleLogoutServices().add(buildSingleLogoutService());
    spssoDescriptor.addSupportedProtocol(SAMLConstants.SAML20P_NS);

    for (Client client : clientsMap.values()) {
      spssoDescriptor.getAttributeConsumingServices().add(buildAttributeConsumingService(client));
    }

    entityDescriptor.setOrganization(buildOrganization());
    entityDescriptor.getContactPersons().add(buildContactPerson());
    entityDescriptor.getRoleDescriptors().add(spssoDescriptor);
    entityDescriptor.getNamespaceManager()
        .registerNamespaceDeclaration(new Namespace(NAMESPACE_URI, NAMESPACE_PREFIX));

    Signature signature = buildSignature(entityDescriptor);
    entityDescriptor.setSignature(signature);
    Marshaller out = XMLObjectProviderRegistrySupport.getMarshallerFactory()
        .getMarshaller(entityDescriptor);

    Element plaintextElement;
    try {
      plaintextElement = out.marshall(entityDescriptor);
    } catch (MarshallingException e) {
      throw new OneIdentityException(e);
    }

    try {
      Signer.signObject(signature);
    } catch (SignatureException e) {
      throw new OneIdentityException(e);
    }

    return Response.ok(getStringValue(plaintextElement)).build();
  }
}
