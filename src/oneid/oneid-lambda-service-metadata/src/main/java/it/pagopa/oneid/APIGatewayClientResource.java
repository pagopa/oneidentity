package it.pagopa.oneid;

import jakarta.enterprise.context.ApplicationScoped;
import software.amazon.awssdk.services.apigateway.ApiGatewayClient;

public class APIGatewayClientResource {

  @ApplicationScoped
  ApiGatewayClient apiGatewayClient() {
    return ApiGatewayClient.create();
  }

}
