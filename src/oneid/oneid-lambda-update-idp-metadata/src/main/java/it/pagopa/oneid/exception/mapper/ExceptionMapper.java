package it.pagopa.oneid.exception.mapper;

import io.quarkus.logging.Log;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

public class ExceptionMapper {

  @ServerExceptionMapper
  public void mapException(NoSuchKeyException noSuchKeyException) {
    Log.error(ExceptionUtils.getStackTrace(
        noSuchKeyException));
  }

}
