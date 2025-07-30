package it.pagopa.oneid.common.model;

import it.pagopa.oneid.common.model.converters.HashMapAttributeConverter;
import it.pagopa.oneid.common.model.enums.AuthLevel;
import it.pagopa.oneid.common.model.enums.converter.AuthLevelConverter;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
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
@Builder
public class Client {


  @Getter(onMethod_ = @DynamoDbPartitionKey)
  @NotNull
  private String clientId;

  //TODO remove the comment below when all is ready, to activate the check
  //@NotNull
  private String userId;

  @NotNull
  private String friendlyName;

  @NotNull
  private Set<String> callbackURI;

  @NotNull
  private Set<String> requestedParameters;


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

  // Fields related to ClientMetadataDTO
  private String logoUri;

  private String policyUri;

  private String tosUri;

  private boolean requiredSameIdp;
  // Fields related to FE
  private String a11yUri;
  private boolean backButtonEnabled;
  @Getter(onMethod_ = @DynamoDbConvertedBy(HashMapAttributeConverter.class))
  private Map<String, Map<String, LocalizedContent>> localizedContentMap;

  public record LocalizedContent(String title, String description, String docUri,
                                 String supportAddress, String cookieUri) {

  }
}
