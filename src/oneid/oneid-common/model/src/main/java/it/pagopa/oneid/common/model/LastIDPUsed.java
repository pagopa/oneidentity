package it.pagopa.oneid.common.model;

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
public class LastIDPUsed {


  @Getter(onMethod_ = @DynamoDbPartitionKey)
  @NotNull
  private String id;

  @Getter(onMethod_ = @DynamoDbSortKey)
  @NotNull
  private String clientId;

  @NotNull
  private String entityId;

  @NotNull
  private long ttl;

}
