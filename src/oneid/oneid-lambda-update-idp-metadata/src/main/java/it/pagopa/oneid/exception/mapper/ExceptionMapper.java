package it.pagopa.oneid.exception.mapper;

import io.quarkus.logging.Log;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

public class ExceptionMapper {

  // TODO: is it okay to use ExceptionMapper in this Lambda?
  @ServerExceptionMapper
  public Response mapException(NoSuchKeyException noSuchKeyException) {
    Log.error(ExceptionUtils.getStackTrace(
        noSuchKeyException));
    return Response.status(Status.INTERNAL_SERVER_ERROR).build();
  }

}
