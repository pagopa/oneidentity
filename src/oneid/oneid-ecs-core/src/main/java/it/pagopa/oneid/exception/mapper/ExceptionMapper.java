package it.pagopa.oneid.exception.mapper;


import it.pagopa.oneid.exception.GenericAuthnRequestCreationException;
import it.pagopa.oneid.exception.IDPSSOEndpointNotFoundException;
import it.pagopa.oneid.model.ErrorResponse;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;
import static jakarta.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

public class ExceptionMapper {

    @ServerExceptionMapper
    public RestResponse<ErrorResponse> mapGenericAuthnRequestCreationException(GenericAuthnRequestCreationException genericAuthnRequestCreationException) {
        Response.Status status = INTERNAL_SERVER_ERROR;
        String message = "Error during generation of AuthnRequest.";
        return RestResponse.status(status, buildErrorResponse(status, message));
    }

    @ServerExceptionMapper
    public RestResponse<ErrorResponse> mapIDPSSOEndpointNotFoundException(IDPSSOEndpointNotFoundException idpssoEndpointNotFoundException) {
        Response.Status status = BAD_REQUEST;
        String message = "IDPSSO endpoint not found for selected idp.";
        return RestResponse.status(status, buildErrorResponse(status, message));
    }

    private ErrorResponse buildErrorResponse(Response.Status status, String message) {
        return ErrorResponse.builder()
                .title(status.getReasonPhrase())
                .status(status.getStatusCode())
                .detail(message)
                .build();
    }
}
