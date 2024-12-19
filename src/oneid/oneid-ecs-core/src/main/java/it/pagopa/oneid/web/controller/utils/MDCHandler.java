package it.pagopa.oneid.web.controller.utils;

import org.jboss.logmanager.MDC;

public class MDCHandler {

  public static void setMDCProperty(MDCProperty property, String value) {
    MDC.put(property.getValue(), value);
  }

  public static void updateMDCClientAndStateProperties(String clientId, String state) {
    // Put Client ID into MDC {client.id} property
    MDCHandler.setMDCProperty(MDCProperty.CLIENT_ID, clientId);
    // Put Client state into MDC {client.state} property
    MDCHandler.setMDCProperty(MDCProperty.CLIENT_STATE, state);

  }

}
