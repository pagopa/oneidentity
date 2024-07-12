package it.pagopa.oneid.web.converters;

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
    if (rawType.isEnum() && rawType.equals(GrantType.class)) {
      return new ParamConverter<T>() {
        @Override
        public T fromString(String value) {
          try {
            return (T) GrantType.valueOf(value.toUpperCase());
          } catch (IllegalArgumentException e) {
            return null;
          }
        }

        @Override
        public String toString(T value) {
          if (value == null) {
            return null;
          }
          return value.toString();
        }
      };
    }
    if (rawType.isEnum() && rawType.equals(ResponseType.class)) {
      return new ParamConverter<T>() {
        @Override
        public T fromString(String value) {
          try {
            return (T) ResponseType.valueOf(value.toUpperCase());
          } catch (IllegalArgumentException e) {
            return null;
          }
        }

        @Override
        public String toString(T value) {
          if (value == null) {
            return null;
          }
          return value.toString();
        }
      };
    }
    return null;
  }
}
