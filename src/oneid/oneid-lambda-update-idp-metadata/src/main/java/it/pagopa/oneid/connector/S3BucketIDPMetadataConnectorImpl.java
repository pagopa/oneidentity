package it.pagopa.oneid.connector;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.IOException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.utils.IoUtils;

@ApplicationScoped
public class S3BucketIDPMetadataConnectorImpl implements S3BucketIDPMetadataConnector {

  @ConfigProperty(name = "idp_metadata.bucket.name")
  String bucketName;

  @Inject
  S3Client s3;

  private GetObjectRequest buildGetRequest(String objectKey) {
    Log.debug("start");
    return GetObjectRequest.builder()
        .bucket(bucketName)
        .key(objectKey)
        .build();
  }

  @Override
  public String getMetadataFile(String fileName) {
    Log.debug("start");
    String result;
    ResponseInputStream<GetObjectResponse> response;
    try {
      response = s3.getObject(buildGetRequest(fileName));
    } catch (S3Exception e) {
      Log.error("error retrieving data from S3: " + e.getMessage());
      throw new RuntimeException(e);
    }
    try {
      result = IoUtils.toUtf8String(response);
    } catch (IOException e) {
      Log.error("error during file reading: " + e.getMessage());
      throw new RuntimeException(e);
    }

    return result;
  }


}
