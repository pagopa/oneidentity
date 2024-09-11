package it.pagopa.oneid.common.model;

import it.pagopa.oneid.common.model.enums.IDPStatus;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import java.util.Set;
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
public class IDP {

  @Getter(onMethod_ = @DynamoDbPartitionKey)
  @NotNull
  private String entityID;

  @Getter(onMethod_ = @DynamoDbSortKey)
  @NotNull
  private String pointer;

  @NotNull
  private long timestamp;

  @NotNull
  private boolean isActive;

  @NotNull
  private IDPStatus status;

  @NotNull
  private Map<String, String> idpSSOEndpoints;

  @NotNull
  private Set<String> certificates;

}
