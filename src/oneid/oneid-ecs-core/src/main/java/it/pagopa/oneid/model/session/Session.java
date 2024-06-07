package it.pagopa.oneid.model.session;

import it.pagopa.oneid.model.session.enums.RecordType;
import lombok.Data;
import lombok.Getter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

@DynamoDbBean
@Data
public abstract class Session {

    @Getter(onMethod_ = @DynamoDbPartitionKey)
    private String SAMLRequestID;

    @Getter(onMethod_ = @DynamoDbSortKey)
    private RecordType recordType;

    private Long creationTime;

    private Long ttl;
}
