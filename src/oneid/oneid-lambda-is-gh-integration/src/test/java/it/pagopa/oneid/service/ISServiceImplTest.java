package it.pagopa.oneid.service;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@QuarkusTest
class ISServiceImplTest {

  @Inject
  ISServiceImpl isServiceImpl;

  @Test
  void getLatestIdpMetadata() {
    String metadata = isServiceImpl.getLatestIdpMetadata("spid");
    Assertions.assertNotNull(metadata);
  }
}