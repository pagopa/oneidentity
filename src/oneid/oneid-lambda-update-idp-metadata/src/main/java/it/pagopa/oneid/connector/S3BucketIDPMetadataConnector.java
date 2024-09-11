package it.pagopa.oneid.connector;

import java.util.Optional;

public interface S3BucketIDPMetadataConnector {

  Optional<String> getMetadataFile(String fileName);

}
