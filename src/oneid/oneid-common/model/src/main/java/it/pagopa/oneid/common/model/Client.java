package it.pagopa.oneid.common.model;

import it.pagopa.oneid.common.model.enums.AuthLevel;
import it.pagopa.oneid.common.model.enums.converter.AuthLevelConverter;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbConvertedBy;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;


@Getter
@DynamoDbBean
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Client {

  @Getter(onMethod_ = @DynamoDbPartitionKey)
  @NotNull
  private String clientId;

  @NotNull
  private String friendlyName;

  @NotNull
  private List<String> callbackURI;

  @NotNull
  private List<String> requestedParameters;


  @Getter(onMethod_ = @DynamoDbConvertedBy(AuthLevelConverter.class))
  @NotNull
  private AuthLevel authLevel;

  @NotNull
  private int acsIndex;

  @NotNull
  private int attributeIndex;

  @NotNull
  private boolean isActive;

  @NotNull
  private long clientIdIssuedAt;


}
