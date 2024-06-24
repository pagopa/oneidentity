package example;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;
import java.util.HashMap;
import java.util.Map;

public class HelloWorld implements RequestHandler<Object, String> {
    @Override
    public String handleRequest(Object input, Context context) {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Hello, World!");

        // Convert the map to a JSON string
        Gson gson = new Gson();
        return gson.toJson(response);
    }
}