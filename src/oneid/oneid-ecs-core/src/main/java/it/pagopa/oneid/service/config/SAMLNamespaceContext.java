package it.pagopa.oneid.service.config;

import java.util.Iterator;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

public class SAMLNamespaceContext implements NamespaceContext {

  @Override
  public String getNamespaceURI(String prefix) {
    if (prefix == null) {
      throw new NullPointerException("Null prefix is not allowed.");
    }
    switch (prefix) {
      case "saml2p":
        return "urn:oasis:names:tc:SAML:2.0:protocol";
      case "saml2":
        return "urn:oasis:names:tc:SAML:2.0:assertion";
      case "ds":
        return "http://www.w3.org/2000/09/xmldsig#";
      default:
        // Return null for unmapped prefixes
        return XMLConstants.NULL_NS_URI;
    }
  }

  @Override
  public String getPrefix(String namespaceURI) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Iterator<String> getPrefixes(String namespaceURI) {
    throw new UnsupportedOperationException();
  }
}
