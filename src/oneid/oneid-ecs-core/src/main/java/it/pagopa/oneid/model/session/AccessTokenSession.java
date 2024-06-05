package it.pagopa.oneid.model.session;

public class AccessTokenSession extends Session {

    // TODO set annotation for DB "code"
    private String accessToken;

    private String idToken; //TODO replace with JWT
}
