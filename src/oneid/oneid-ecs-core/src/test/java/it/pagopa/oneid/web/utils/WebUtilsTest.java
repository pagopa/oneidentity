package it.pagopa.oneid.web.utils;

import static it.pagopa.oneid.web.utils.WebUtils.getElementValueFromAuthnRequest;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import it.pagopa.oneid.service.SAMLServiceImpl;
import it.pagopa.oneid.service.mock.X509CredentialTestProfile;
import jakarta.inject.Inject;
import javax.xml.parsers.DocumentBuilderFactory;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@QuarkusTest
@TestProfile(X509CredentialTestProfile.class)
class WebUtilsTest {

  @Inject
  private SAMLServiceImpl samlServiceImpl;
  private Document document;

  @BeforeEach
  @SneakyThrows
  void setUp() {
    // Initialize a simple XML Document for testing
    document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

  }

  @Test
  void getStringValue_success() {
    // given
    Element element = document.createElement("TestElement");
    element.setTextContent("test text");

    // when
    String result = WebUtils.getStringValue(element);

    // then
    assertNotNull(result);
    assertEquals(
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestElement>test text</TestElement>",
        result.trim());
  }

  @Test
  @SneakyThrows
  void getElementValueFromAuthnRequest_success() {
    // given
    //TODO remove authnRequest creation based on samlServiceImpl method
    AuthnRequest authnRequest = samlServiceImpl.buildAuthnRequest(
        "https://id.lepida.it/idp/shibboleth",
        0, 0, "testLevel");

    // then
    assertDoesNotThrow(() -> getElementValueFromAuthnRequest(authnRequest));
  }
}