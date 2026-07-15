package it.pagopa.oneid.service;	

import it.pagopa.oneid.common.model.IDP;	
import java.util.ArrayList;	
import java.util.Optional;	

//TODO: remove this service after idps.json is available in the assets bucket.
public interface IdpService {	

  Optional<ArrayList<IDP>> findAllIdpByTimestamp();	

}	
