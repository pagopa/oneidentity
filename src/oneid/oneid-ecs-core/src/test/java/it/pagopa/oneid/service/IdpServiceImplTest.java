package it.pagopa.oneid.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import it.pagopa.oneid.common.connector.IDPConnectorImpl;
import it.pagopa.oneid.common.model.IDP;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@QuarkusTest
class IdpServiceImplTest {

  @Inject
  IdpServiceImpl idpServiceImpl;

  @InjectMock
  IDPConnectorImpl idpConnectorImpl;

  @Test
  void findAllIdpByTimestamp() {

    //given
    ArrayList<IDP> idps = Mockito.mock(ArrayList.class);

    //when
    Mockito.when(idpConnectorImpl.findIDPsByTimestamp(Mockito.anyString()))
        .thenReturn(Optional.of(idps));

    //then
    assertNotNull(idpServiceImpl.findAllIdpByTimestamp());
  }
}