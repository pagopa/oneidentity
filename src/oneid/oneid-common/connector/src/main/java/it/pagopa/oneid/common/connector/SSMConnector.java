package it.pagopa.oneid.common.connector;

import java.util.Optional;

public interface SSMConnector {

  Optional<String> getParameter(String parameterName);

}
