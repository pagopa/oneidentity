package it.pagopa.oneid.model.session;

import lombok.Data;
import lombok.EqualsAndHashCode;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

@EqualsAndHashCode(callSuper = true)
@DynamoDbBean
@Data
public class SAMLSession extends Session {

    private String SAMLRequest; //TODO change in ?

    private String SAMLResponse; //TODO change in ?
}