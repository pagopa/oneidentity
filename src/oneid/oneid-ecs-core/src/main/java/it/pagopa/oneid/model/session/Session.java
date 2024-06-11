package it.pagopa.oneid.model.session;

import it.pagopa.oneid.model.session.enums.RecordType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

@DynamoDbBean
@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class Session {

    @Getter(onMethod_ = @DynamoDbPartitionKey)
    @NotNull
    private String SAMLRequestID;

    @Getter(onMethod_ = @DynamoDbSortKey)
    @NotNull
    private RecordType recordType;

    @NotNull
    private long creationTime;

    @NotNull
    private long ttl;
}
