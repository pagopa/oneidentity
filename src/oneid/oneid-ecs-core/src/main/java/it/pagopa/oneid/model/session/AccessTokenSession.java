package it.pagopa.oneid.model.session;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;

@EqualsAndHashCode(callSuper = true)
@DynamoDbBean
@Data
public class AccessTokenSession extends Session {

  // TODO indexNames by constant
  @Getter(onMethod_ = {
      @DynamoDbSecondaryPartitionKey(indexNames = "gsi_code_idx"),
      @DynamoDbAttribute("code")}
  )
  private String accessToken;

  private String idToken;
}
