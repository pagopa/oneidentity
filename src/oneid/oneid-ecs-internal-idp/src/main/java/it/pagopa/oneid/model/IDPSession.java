package it.pagopa.oneid.model;

import it.pagopa.oneid.model.enums.IDPSessionStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

@Getter
@DynamoDbBean
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IDPSession {

  @Getter(onMethod_ = @DynamoDbPartitionKey)
  @NotNull
  private String authnRequestId;

  @Getter(onMethod_ = @DynamoDbSortKey)
  @NotNull
  private String clientId;

  @NotNull
  private String username;

  @NotNull
  private long timestampStart;

  @NotNull
  private long timestampEnd;

  @NotNull
  private IDPSessionStatus status;
}
