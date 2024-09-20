package it.pagopa.oneid.common.connector;

import it.pagopa.oneid.common.model.IDP;
import it.pagopa.oneid.common.model.enums.LatestTAG;
import java.util.ArrayList;
import java.util.Optional;

public interface IDPConnector {

  Optional<ArrayList<IDP>> findIDPsByTimestamp(String timestamp);

  Optional<IDP> getIDPByEntityIDAndTimestamp(String entityID, String timestamp);

  void saveIDPs(ArrayList<IDP> idpList, LatestTAG latestTag, String timestamp);

}
