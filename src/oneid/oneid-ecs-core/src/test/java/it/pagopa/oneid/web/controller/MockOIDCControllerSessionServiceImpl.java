package it.pagopa.oneid.web.controller;

import it.pagopa.oneid.exception.SessionException;
import it.pagopa.oneid.model.session.SAMLSession;
import it.pagopa.oneid.model.session.Session;
import it.pagopa.oneid.model.session.enums.RecordType;
import it.pagopa.oneid.model.session.enums.ResponseType;
import it.pagopa.oneid.service.SessionServiceImpl;
import it.pagopa.oneid.web.dto.AuthorizationRequestDTOExtended;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Alternative;

@Alternative
@Priority(1)
@Dependent
public class MockOIDCControllerSessionServiceImpl<T extends Session> extends SessionServiceImpl<T> {

  public MockOIDCControllerSessionServiceImpl() {
    super();
  }

  @Override
  public void saveSession(T session) {
    // do nothing
  }

  @Override
  public T getSession(String id, RecordType recordType) throws SessionException {
    SAMLSession session = new SAMLSession();
    return (T) session;
  }

  @Override
  public void setSAMLResponse(String samlRequestID, String response) {
    // do nothing
  }

  @Override
  public String getSAMLResponseByCode(String code) throws SessionException {
    return "samlResponse test";
  }

  @Override
  public SAMLSession getSAMLSessionByCode(String code) {

    String SAMLRequestID = "exampleRequestId";
    RecordType recordType = RecordType.SAML;
    long creationTime = System.currentTimeMillis() / 1000L;
    long ttl = creationTime + (2 * 24 * 60 * 60);
    String SAMLRequest = "exampleSAMLRequest";
    String SAMLResponse = "exampleSAMLResponse";

    // Construct AuthorizationRequestDTOExtended
    AuthorizationRequestDTOExtended authorizationRequestDTOExtended = AuthorizationRequestDTOExtended.builder()
        .idp("exampleIdp")
        .clientId("testClientId")
        .responseType(ResponseType.CODE)
        .redirectUri("https://client.example.com/callback")
        .scope("openid")
        .nonce("exampleNonce")
        .state("exampleState")
        .ipAddress("192.168.1.1")
        .build();

    // Construct SAMLSession
    SAMLSession samlSession = new SAMLSession(
        SAMLRequestID,
        recordType,
        creationTime,
        ttl,
        SAMLRequest,
        authorizationRequestDTOExtended
    );

    samlSession.setSAMLResponse(SAMLResponse);
    return samlSession;
  }


}
