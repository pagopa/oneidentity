package example;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.json.JSONObject;

public class HelloWorld implements RequestHandler<Object, String> {
    @Override
    public APIGatewayProxyResponseEvent handleRequest(Object input, Context context) {
        JSONObject responseBody = new JSONObject();
        responseBody.put("message", "Hello, World!");

        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setStatusCode(200);
        response.setHeaders(Map.of("Content-Type", "application/json"));
        response.setBody(responseBody.toString());

        return response;
    }
}