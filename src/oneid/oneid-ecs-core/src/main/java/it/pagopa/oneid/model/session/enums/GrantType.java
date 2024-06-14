package it.pagopa.oneid.model.session.enums;

import lombok.Getter;

@Getter
public enum GrantType {
    PASSWORD("password"),
    REFRESH_TOKEN("refresh_token"),
    AUTHORIZATION_CODE("authorization_code"),
    CLIENT_CREDENTIALS("client_credentials"),
    JWT_BEARER("urn:ietf:params:oauth:grant-type:jwt-bearer"),
    SAML2_BEARER("urn:ietf:params:oauth:grant-type:saml2-bearer");

    private final String value;

    GrantType(String value) {
        this.value = value;
    }

}
