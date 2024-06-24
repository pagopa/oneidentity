package example;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.json.JSONObject;

public class HelloWorld implements RequestHandler<Object, String> {
    @Override
    public String handleRequest(Object input, Context context) {
        JSONObject obj = new JSONObject();
        JSONObject obj2 = new JSONObject();
        obj2.put("Content-Type", "application/json");
        obj.put("statusCode", 200);
        obj.put("headers", obj2);
        obj.put("body", "hello world");
        return obj.toString();
    }
}
