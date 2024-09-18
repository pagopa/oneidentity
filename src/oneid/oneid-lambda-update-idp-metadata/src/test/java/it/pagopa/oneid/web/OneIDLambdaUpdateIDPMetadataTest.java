package it.pagopa.oneid.web;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification.S3Entity;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification.S3EventNotificationRecord;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification.S3ObjectEntity;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import it.pagopa.oneid.common.model.IDP;
import it.pagopa.oneid.service.IDPMetadataServiceImpl;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@QuarkusTest
public class OneIDLambdaUpdateIDPMetadataTest {

  @InjectMock
  IDPMetadataServiceImpl idPMetadataServiceImpl;

  @Inject
  OneIDLambdaUpdateIDPMetadata oneIDLambdaUpdateIDPMetadata;

  @Test
  void handleRequest_spidIDP() {

    // given

    Context context = Mockito.mock(Context.class);
    S3Event s3Event = Mockito.mock(S3Event.class);
    S3EventNotificationRecord s3EventNotificationRecord = Mockito.mock(
        S3EventNotificationRecord.class);

    S3Entity s3Entity = Mockito.mock(S3Entity.class);
    S3ObjectEntity s3ObjectEntity = Mockito.mock(S3ObjectEntity.class);
    Mockito.when(s3ObjectEntity.getUrlDecodedKey()).thenReturn("spid-11111.xml");
    Mockito.when(s3Entity.getObject()).thenReturn(s3ObjectEntity);
    Mockito.when(s3EventNotificationRecord.getS3()).thenReturn(s3Entity);
    Mockito.when(s3Event.getRecords()).thenReturn(List.of(s3EventNotificationRecord));

    Mockito.when(idPMetadataServiceImpl.getMetadataFile(Mockito.any())).thenReturn("dummy");

    ArrayList<IDP> idps = new ArrayList<>(Collections.singleton(new IDP()));

    Mockito.when(idPMetadataServiceImpl.parseIDPMetadata(Mockito.any(), Mockito.any()))
        .thenReturn(idps);
    Mockito.doNothing().when(idPMetadataServiceImpl)
        .updateIDPMetadata(Mockito.any(), Mockito.any());

    // then
    oneIDLambdaUpdateIDPMetadata.handleRequest(s3Event, context);

  }

  @Test
  void handleRequest_cieIDP() {

    // given

    Context context = Mockito.mock(Context.class);
    S3Event s3Event = Mockito.mock(S3Event.class);
    S3EventNotificationRecord s3EventNotificationRecord = Mockito.mock(
        S3EventNotificationRecord.class);

    S3Entity s3Entity = Mockito.mock(S3Entity.class);
    S3ObjectEntity s3ObjectEntity = Mockito.mock(S3ObjectEntity.class);
    Mockito.when(s3ObjectEntity.getUrlDecodedKey()).thenReturn("cie-11111.xml");
    Mockito.when(s3Entity.getObject()).thenReturn(s3ObjectEntity);
    Mockito.when(s3EventNotificationRecord.getS3()).thenReturn(s3Entity);
    Mockito.when(s3Event.getRecords()).thenReturn(List.of(s3EventNotificationRecord));

    Mockito.when(idPMetadataServiceImpl.getMetadataFile(Mockito.any())).thenReturn("dummy");

    ArrayList<IDP> idps = new ArrayList<>(Collections.singleton(new IDP()));

    Mockito.when(idPMetadataServiceImpl.parseIDPMetadata(Mockito.any(), Mockito.any()))
        .thenReturn(idps);
    Mockito.doNothing().when(idPMetadataServiceImpl)
        .updateIDPMetadata(Mockito.any(), Mockito.any());

    // then
    oneIDLambdaUpdateIDPMetadata.handleRequest(s3Event, context);

  }
}
