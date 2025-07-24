package it.pagopa.oneid.connector;

import java.util.Optional;

public interface CognitoConnector {

  Optional<String> extractClientIdByUserId(String userId);

}
