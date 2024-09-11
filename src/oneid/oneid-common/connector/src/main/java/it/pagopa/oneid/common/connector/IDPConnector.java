package it.pagopa.oneid.common.connector;

import it.pagopa.oneid.common.model.IDP;
import it.pagopa.oneid.common.model.LatestItem;
import java.util.ArrayList;
import java.util.Optional;

public interface IDPConnector {

  Optional<ArrayList<IDP>> findIDPsByTimestamp(long timestamp);

  Optional<IDP> getLatestIDPVersionByEntityIDAndTimestamp(String entityID, long timestamp);

  void saveIDPs(ArrayList<IDP> idpList, LatestItem latestItem);

  Optional<LatestItem> getLatestItem();


}
