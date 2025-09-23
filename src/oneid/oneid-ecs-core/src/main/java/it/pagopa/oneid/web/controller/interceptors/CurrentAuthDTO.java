package it.pagopa.oneid.web.controller.interceptors;

import it.pagopa.oneid.model.session.SAMLSession;
import jakarta.enterprise.context.RequestScoped;
import lombok.Data;
import org.opensaml.saml.saml2.core.Response;

@RequestScoped
@Data
public class CurrentAuthDTO {

  Response response;

  SAMLSession samlSession;

  boolean ResponseWithMultipleSignatures;

}
