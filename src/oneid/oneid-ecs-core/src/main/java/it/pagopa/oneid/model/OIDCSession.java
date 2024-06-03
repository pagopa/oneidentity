package it.pagopa.oneid.model;

import it.pagopa.oneid.common.RecordType;

import java.util.UUID;

public class OIDCSession {

    private UUID SAMLRequestID;

    private RecordType recordType;

    private Long creationTime;

    private Long ttl;

    private String code;

}
