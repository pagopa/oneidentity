package it.pagopa.oneid.model.session;

import it.pagopa.oneid.model.session.enums.RecordType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

@EqualsAndHashCode(callSuper = true)
@DynamoDbBean
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SAMLSession extends Session {

  // TODO consider adding state, scope and nonce fields without relying on RelayState
  @NotNull
  private String SAMLRequest;

  // TODO: evaluate if this is better than throwing an exception on update
  // @Getter(onMethod_ = @DynamoDbUpdateBehavior(UpdateBehavior.WRITE_IF_NOT_EXISTS))
  private String SAMLResponse;

  public SAMLSession(@NotNull String SAMLRequestID, @NotNull RecordType recordType,
      @NotNull long creationTime, @NotNull long ttl, String SAMLRequest) {
    super(SAMLRequestID, recordType, creationTime, ttl);
    this.SAMLRequest = SAMLRequest;
  }
}