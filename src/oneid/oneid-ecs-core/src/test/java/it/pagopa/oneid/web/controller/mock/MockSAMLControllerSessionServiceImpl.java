package it.pagopa.oneid.web.controller.mock;

import it.pagopa.oneid.exception.SessionException;
import it.pagopa.oneid.model.Base64SAMLResponses;
import it.pagopa.oneid.model.session.SAMLSession;
import it.pagopa.oneid.model.session.Session;
import it.pagopa.oneid.model.session.enums.RecordType;
import it.pagopa.oneid.model.session.enums.ResponseType;
import it.pagopa.oneid.service.SessionServiceImpl;
import it.pagopa.oneid.web.dto.AuthorizationRequestDTOExtended;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Alternative;
import javax.annotation.Priority;

@Alternative
@Priority(1)
@Dependent
public class MockSAMLControllerSessionServiceImpl<T extends Session> extends
    SessionServiceImpl<T> {

  public MockSAMLControllerSessionServiceImpl() {
    super();
  }

  @Override
  public void saveSession(T session) throws SessionException {
    if (session != null && session.getSamlRequestID() != null && session.getSamlRequestID()
        .equals("exceptionOIDC")) {
      throw new SessionException();
    }
  }

  @Override
  public T getSession(String id, RecordType recordType) throws SessionException {
    if (id != null && id.equals("exceptionSAML")) {
      throw new SessionException();
    }
    String dummySAMLRequest = "dummySAMLRequest";
    String dummySAMLResponse = "dummySAMLResponse";
    AuthorizationRequestDTOExtended dummyAuthorizationRequestDTOExtended = new AuthorizationRequestDTOExtended(
        "test", "test", ResponseType.CODE, "test", "test", "test", "test", "test");

    return (T) new SAMLSession(dummySAMLRequest, dummySAMLResponse,
        dummyAuthorizationRequestDTOExtended);
  }

  @Override
  public void setSAMLResponse(String samlRequestID, String response) throws SessionException {
    if (samlRequestID != null && samlRequestID.equals("exceptionSAMLResponse")) {
      throw new SessionException();
    }
  }

  @Override
  public SAMLSession getSAMLSessionByCode(String code) throws SessionException {
    return super.getSAMLSessionByCode(code);
  }

  @Override
  public String getSAMLResponseByCode(String code) throws SessionException {
    if (code.equals("exceptionAccessToken")) {
      throw new SessionException();
    }
    return Base64SAMLResponses.CORRECT_SAML_RESPONSE_01;
  }


}
