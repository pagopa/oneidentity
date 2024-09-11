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
public class LatestItem {

  public static final String LATEST_ITEM_PK = "LATEST";
  public static final long LATEST_ITEM_RK = 0;

  @Getter(onMethod_ = @DynamoDbPartitionKey)
  @NotNull
  private String entityID = LATEST_ITEM_PK;

  @Getter(onMethod_ = @DynamoDbSortKey)
  @NotNull
  private long timestamp = LATEST_ITEM_RK;

  @NotNull
  private long latestTimestampSPID;

  @NotNull
  private long latestTimestampCIE;
}

