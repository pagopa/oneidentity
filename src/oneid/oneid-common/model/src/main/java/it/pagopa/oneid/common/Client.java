package it.pagopa.oneid.common;

import lombok.Data;
import org.opensaml.saml.saml2.metadata.RequestedAttribute;

import java.util.ArrayList;

@Data
public class Client {

    private String clientId;

    private String friendlyName;

    private ArrayList<String> callbackURI;

    private ArrayList<RequestedAttribute> requestedParameters;

    private int acsIndex;

    private int attributeIndex;

    private boolean isActive;
}
