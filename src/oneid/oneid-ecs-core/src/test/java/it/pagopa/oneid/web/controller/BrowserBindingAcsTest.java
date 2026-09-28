package it.pagopa.oneid.web.controller;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import it.pagopa.oneid.connector.CloudWatchConnectorImpl;
import it.pagopa.oneid.exception.GenericHTMLException;
import it.pagopa.oneid.model.session.SAMLSession;
import it.pagopa.oneid.service.BrowserBindingService;
import it.pagopa.oneid.service.SessionServiceImpl;
import it.pagopa.oneid.web.controller.interceptors.CurrentAuthDTO;
import it.pagopa.oneid.web.dto.SAMLResponseDTO;
import jakarta.ws.rs.core.HttpHeaders;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BrowserBindingAcsTest {

    @Test
    @DisplayName("Enforcement rejects an unbound ACS before writing the SAML response")
    @SuppressWarnings("unchecked")
    void givenMissingBinding_whenEnforcingAcs_thenNoSessionMutation() {
        SAMLController controller = new SAMLController();
        controller.browserBindingService = mock(BrowserBindingService.class);
        controller.cloudWatchConnectorImpl = mock(CloudWatchConnectorImpl.class);
        controller.samlSessionService = mock(SessionServiceImpl.class);
        controller.httpHeaders = mock(HttpHeaders.class);
        controller.currentAuthDTO = new CurrentAuthDTO();
        controller.currentAuthDTO.setSamlSession(mock(SAMLSession.class));
        controller.currentAuthDTO.setResponse(mock(org.opensaml.saml.saml2.core.Response.class));
        when(controller.httpHeaders.getCookies()).thenReturn(Map.of());
        when(controller.browserBindingService.enabled()).thenReturn(true);
        when(controller.browserBindingService.enforcing()).thenReturn(true);
        when(controller.browserBindingService.mode()).thenReturn(BrowserBindingService.Mode.ENFORCE);
        when(controller.browserBindingService.verify(controller.currentAuthDTO.getSamlSession(), Map.of()))
                .thenReturn(BrowserBindingService.Outcome.MISSING);

        assertThrows(GenericHTMLException.class, () -> controller.samlACS(mock(SAMLResponseDTO.class)));
        verify(controller.cloudWatchConnectorImpl).sendBrowserBindingMetricData("MISSING");
        verifyNoInteractions(controller.samlSessionService);
    }
}
