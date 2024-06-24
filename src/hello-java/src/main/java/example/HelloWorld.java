package example;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.json.JSONObject;

public class HelloWorld implements RequestHandler<Object, String> {
    @Override
    public String handleRequest(Object input, Context context) {
        JSONObject response = new JSONObject();
        response.put("message", "Hello, World!");

        return response.toString();
    }
}