package it.pagopa.oneid.model.session;

import it.pagopa.oneid.model.session.enums.RecordType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;

@EqualsAndHashCode(callSuper = true)
@DynamoDbBean
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OIDCSession extends Session {

  // TODO indexNames by constant
  @Getter(onMethod_ = @DynamoDbSecondaryPartitionKey(indexNames = "gsi_code_idx"))
  private String authorizationCode;

  private String nonce;

  public OIDCSession(@NotNull String SAMLRequestID, @NotNull RecordType recordType,
      @NotNull long creationTime, @NotNull long ttl, String authorizationCode, String nonce) {
    super(SAMLRequestID, recordType, creationTime, ttl);
    this.authorizationCode = authorizationCode;
    this.nonce = nonce;
  }

}
