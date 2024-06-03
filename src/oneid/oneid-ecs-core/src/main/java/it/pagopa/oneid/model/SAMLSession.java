package it.pagopa.oneid.model;

import it.pagopa.oneid.common.RecordType;

import java.util.UUID;

public class SAMLSession {

    private UUID SAMLRequestID;

    private RecordType recordType;

    private Long creationTime;

    private Long ttl;

    private String SAMLRequest; //TODO change in XML

    private String SAMLResponse; //TODO change in XML

}