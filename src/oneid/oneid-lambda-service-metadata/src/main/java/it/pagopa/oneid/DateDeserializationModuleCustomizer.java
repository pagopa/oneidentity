package it.pagopa.oneid;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.quarkus.jackson.ObjectMapperCustomizer;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.util.Date;

@Singleton
public class DateDeserializationModuleCustomizer implements ObjectMapperCustomizer {

  @Override
  public void customize(ObjectMapper objectMapper) {
    SimpleModule dateDeserializationModule = new SimpleModule();
    dateDeserializationModule.addDeserializer(Date.class, new JsonDeserializer<>() {
      @Override
      public Date deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
          throws IOException {
        double approximateCreationDateTimeInSeconds = jsonParser.getValueAsDouble();
        return new Date((long) (approximateCreationDateTimeInSeconds * 1000));
      }
    });
    objectMapper.registerModule(dateDeserializationModule);
  }
}