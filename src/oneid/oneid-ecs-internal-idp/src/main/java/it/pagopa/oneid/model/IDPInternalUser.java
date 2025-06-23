package it.pagopa.oneid.model;

import jakarta.validation.constraints.NotNull;
import java.util.Map;
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
public class IDPInternalUser {

  @Getter(onMethod_ = @DynamoDbPartitionKey)
  @NotNull
  private String username;

  @Getter(onMethod_ = @DynamoDbSortKey)
  @NotNull
  private String namespace;

  @NotNull
  private String password;

  @NotNull
  private Map<String, String> samlAttributes;
}
