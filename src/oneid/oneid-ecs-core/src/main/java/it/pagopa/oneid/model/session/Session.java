package it.pagopa.oneid.model.session;

import it.pagopa.oneid.model.session.enums.RecordType;
import lombok.Data;

@Data
public class Session {
    private String SAMLRequestID;

    private RecordType recordType;

    private Long creationTime;

    private Long ttl;
}
