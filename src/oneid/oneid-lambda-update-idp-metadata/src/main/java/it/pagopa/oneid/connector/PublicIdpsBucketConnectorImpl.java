package it.pagopa.oneid.connector;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.logging.Log;
import it.pagopa.oneid.common.utils.logging.CustomLogging;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@ApplicationScoped
@CustomLogging
public class PublicIdpsBucketConnectorImpl implements PublicIdpsBucketConnector {

  @ConfigProperty(name = "assets.bucket.name")
  String assetsBucketName;

  @Inject
  S3Client s3;

  @Override
  public void uploadIdpsJson(String objectKey, String content) {
    PutObjectRequest putObjectRequest = PutObjectRequest.builder()
        .bucket(assetsBucketName)
        .contentType("application/json")
        .key(objectKey)
        .build();

    try {
      s3.putObject(putObjectRequest, RequestBody.fromString(content));
    } catch (S3Exception e) {
      Log.error("error during public IDPs snapshot upload: " + e.getMessage());
      throw new RuntimeException(e);
    }
  }
}
