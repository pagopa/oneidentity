package it.pagopa.oneid.model.session;

import lombok.Data;

@Data
public class SAMLSession extends Session {

    private String SAMLRequest; //TODO change in ?

    private String SAMLResponse; //TODO change in ?
}