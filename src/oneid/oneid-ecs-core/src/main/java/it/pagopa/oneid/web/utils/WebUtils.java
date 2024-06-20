package it.pagopa.oneid.web.utils;

import com.nimbusds.jose.shaded.gson.JsonObject;
import com.nimbusds.jose.shaded.gson.JsonParser;
import io.quarkus.logging.Log;
import it.pagopa.oneid.model.dto.AuthorizationRequestDTO;
import java.io.StringWriter;
import java.util.Base64;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
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
    Log.debug("[WebUtils.getRelayState] start");
    JsonObject relayState = new JsonObject();

    relayState.addProperty("response_type", authorizationRequestDTO.getResponseType().getValue());
    relayState.addProperty("scope", authorizationRequestDTO.getScope());
    relayState.addProperty("client_id", authorizationRequestDTO.getClientId());
    relayState.addProperty("state", authorizationRequestDTO.getState());
    relayState.addProperty("nonce", authorizationRequestDTO.getNonce());
    relayState.addProperty("callback_uri", authorizationRequestDTO.getRedirectUri());

    return relayState.toString();
  }

  public static JsonObject getJSONRelayState(String relayState) {
    Log.debug("[WebUtils.getJSONRelayState] start");
    return JsonParser.parseString(new String(Base64.getDecoder().decode(relayState.getBytes())))
        .getAsJsonObject();
  }


}
