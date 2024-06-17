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

  @NotNull
  private String SAMLRequest;

  private String SAMLResponse;

  public SAMLSession(@NotNull String SAMLRequestID, @NotNull RecordType recordType,
      @NotNull long creationTime, @NotNull long ttl, String SAMLRequest) {
    super(SAMLRequestID, recordType, creationTime, ttl);
    this.SAMLRequest = SAMLRequest;
  }
}