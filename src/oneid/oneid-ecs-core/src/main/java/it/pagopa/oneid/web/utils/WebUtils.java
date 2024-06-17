package it.pagopa.oneid.web.utils;

import it.pagopa.oneid.model.dto.AuthorizationRequestDTO;
import java.io.StringWriter;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import net.minidev.json.JSONObject;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.Marshaller;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.w3c.dom.Element;

public class WebUtils {


  public static String getStringValue(Element element) {
    StreamResult result = new StreamResult(new StringWriter());
    try {
      TransformerFactory
          .newInstance()
          .newTransformer()
          .transform(new DOMSource(element), result);
    } catch (TransformerException e) {
      throw new RuntimeException(e);
    }
    return result.getWriter().toString();
  }

  public static Element getElementValueFromAuthnRequest(AuthnRequest authnRequest) {
    Marshaller out = XMLObjectProviderRegistrySupport.getMarshallerFactory()
        .getMarshaller(authnRequest);

    Element plaintextElement = null;
    try {
      plaintextElement = out.marshall(authnRequest);
    } catch (MarshallingException e) {
      throw new RuntimeException(e);
    }

    return plaintextElement;
  }

  public static String getRelayState(AuthorizationRequestDTO authorizationRequestDTO) {
    JSONObject relayState = new JSONObject();

    relayState.put("response_type", authorizationRequestDTO.getResponseType());
    relayState.put("scope", authorizationRequestDTO.getScope());
    relayState.put("client_id", authorizationRequestDTO.getClientId());
    relayState.put("state", authorizationRequestDTO.getState());
    relayState.put("nonce", authorizationRequestDTO.getNonce());
    relayState.put("callback_uri", authorizationRequestDTO.getRedirectUri());

    return relayState.toJSONString();
  }


}
