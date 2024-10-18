package it.pagopa.oneid.web.converter;

import it.pagopa.oneid.common.model.enums.GrantType;
import it.pagopa.oneid.model.session.enums.ResponseType;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.ext.ParamConverter;
import jakarta.ws.rs.ext.ParamConverterProvider;
import jakarta.ws.rs.ext.Provider;

@Provider
@ApplicationScoped
public class StringToEnumConverterProvider implements ParamConverterProvider {

  @SuppressWarnings("unchecked")
  @Override
  public <T> ParamConverter<T> getConverter(Class<T> rawType, java.lang.reflect.Type genericType,
      java.lang.annotation.Annotation[] annotations) {
    if (rawType.equals(GrantType.class) || rawType.equals(ResponseType.class)) {
      return createEnumConverter(rawType);
    }
    return null;
  }

  private <T> ParamConverter<T> createEnumConverter(Class<T> enumType) {
    return new ParamConverter<T>() {
      @Override
      public T fromString(String value) {
        try {
          return (T) Enum.valueOf((Class<Enum>) enumType, value.toUpperCase());
        } catch (IllegalArgumentException e) {
          return null;
        }
      }

      @Override
      public String toString(T value) {
        return value == null ? null : value.toString();
      }
    };
  }
}
