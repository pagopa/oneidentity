package it.pagopa.oneid.common;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

import java.util.List;

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

    @NotNull
    private int acsIndex;

    @NotNull
    private int attributeIndex;

    @NotNull
    private boolean isActive;
}
