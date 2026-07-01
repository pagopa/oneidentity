package it.pagopa.oneid;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import io.quarkus.logging.Log;
import jakarta.inject.Named;
import java.util.Map;

/**
 * Stub Lambda handler for the cache-updater function.
 * Receives DynamoDB Stream events forwarded by EventBridge Pipes
 * and will update the ElastiCache Valkey client-configuration cache.
 *
 * Implementation is intentionally empty: this class is the package
 * skeleton that allows infrastructure provisioning to complete independently
 * of the business logic, which will be added in a dedicated branch.
 */
@Named("cache-updater")
public class CacheUpdaterHandler implements RequestHandler<Map<String, Object>, Void> {

    @Override
    public Void handleRequest(Map<String, Object> event, Context context) {
        Log.info("CacheUpdaterHandler invoked — stub, no-op.");
        return null;
    }
}
