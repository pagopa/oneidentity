package it.pagopa.oneid.model.session;

import lombok.Data;

@Data
public class OIDCSession extends Session {
    // TODO set annotation for DB "code"
    private String authorizationCode;

}
