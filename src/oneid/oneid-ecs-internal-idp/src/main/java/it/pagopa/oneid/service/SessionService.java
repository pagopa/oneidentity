package it.pagopa.oneid.service;

public interface SessionService {

  void validateAuthnRequestIdCookie(String authnRequestId, String username);


}
