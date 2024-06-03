package it.pagopa.oneid.model;

import java.util.UUID;

public class AccessTokenSession {

    private UUID SAMLRequestID;

    private RecordType recordType;

    private Long creationTime;

    private Long ttl;

    private String code;

    private String idToken; //TODO replace with JWT
}
