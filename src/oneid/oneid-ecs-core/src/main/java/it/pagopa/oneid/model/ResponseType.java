package it.pagopa.oneid.model;

public enum ResponseType {
    CODE("code"),
    TOKEN("token"),
    TOKEN_ID_TOKEN("token id_token"),
    CODE_ID_TOKEN("code id_token"),
    ID_TOKEN("id_token"),
    CODE_TOKEN("code token"),
    CODE_ID_TOKEN_TOKEN("code id_token token"),
    NONE("none");

    private String value;

    ResponseType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}