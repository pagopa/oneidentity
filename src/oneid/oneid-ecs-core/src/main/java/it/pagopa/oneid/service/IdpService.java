package it.pagopa.oneid.service;

import it.pagopa.oneid.common.model.IDP;
import java.util.ArrayList;
import java.util.Optional;

public interface IdpService {

  Optional<ArrayList<IDP>> findAllIdpByTimestamp();

}
