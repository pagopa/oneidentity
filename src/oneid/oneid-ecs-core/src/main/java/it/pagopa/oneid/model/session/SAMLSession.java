package it.pagopa.oneid.model.session;

import it.pagopa.oneid.model.session.enums.RecordType;
import it.pagopa.oneid.web.dto.AuthorizationRequestDTOExtended;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbFlatten;

@EqualsAndHashCode(callSuper = true)
@DynamoDbBean
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SAMLSession extends Session {

  @NotNull
  private String SAMLRequest;

  // TODO: evaluate if this is better than throwing an exception on update
  // @Getter(onMethod_ = @DynamoDbUpdateBehavior(UpdateBehavior.WRITE_IF_NOT_EXISTS))
  private String SAMLResponse;

  @Getter(onMethod_ = @DynamoDbFlatten)
  private AuthorizationRequestDTOExtended authorizationRequestDTOExtended;

  public SAMLSession(@NotNull String SAMLRequestID, @NotNull RecordType recordType,
      @NotNull long creationTime, @NotNull long ttl, String SAMLRequest,
      AuthorizationRequestDTOExtended authorizationRequestDTOExtended) {
    super(SAMLRequestID, recordType, creationTime, ttl);
    this.SAMLRequest = SAMLRequest;
    this.authorizationRequestDTOExtended = authorizationRequestDTOExtended;
  }
}