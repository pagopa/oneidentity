package it.pagopa.oneid.connector;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@QuarkusTest
public class S3BucketIDPMetadataConnectorImplTest {

  @Inject
  S3BucketIDPMetadataConnectorImpl s3BucketIDPMetadataConnectorImpl;

  @ConfigProperty(name = "idp_metadata.bucket.name")
  String bucketName;

  @Inject
  S3Client s3;

  @BeforeEach
  public void beforeEach() {
    // Here we create the -> {idp_metadata.bucket.name} bucket
    String fileName = "bogus.xml";
    // get the file url, not working in JAR file.
    URL resource = getClass().getClassLoader().getResource(fileName);
    if (resource == null) {
      throw new IllegalArgumentException("file not found!");
    }
    s3.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
    try {
      s3.putObject(PutObjectRequest.builder().bucket(bucketName).key(fileName).build(),
          new File(resource.toURI()).toPath());
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }

  }

  @AfterEach
  public void afterEach() {
    // Here we delete the -> {idp_metadata.bucket.name} bucket
    String fileName = "bogus.xml";

    s3.deleteObject(DeleteObjectRequest.builder().bucket(bucketName).key(fileName).build());
    s3.deleteBucket(DeleteBucketRequest.builder().bucket(bucketName).build());
  }

  @Test
  void getMetadataFile_OK() {
    String fileName = "bogus.xml";
    String content = s3BucketIDPMetadataConnectorImpl.getMetadataFile(fileName);

    assertFalse(content.isBlank());
  }

  @Test
  void getMetadataFile_NotExistingFile() {
    String fileName = "dummy.xml";

    assertThrows(RuntimeException.class,
        () -> s3BucketIDPMetadataConnectorImpl.getMetadataFile(fileName));
  }

}
