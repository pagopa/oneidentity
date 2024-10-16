package it.pagopa.oneid.web.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import io.quarkus.test.junit.QuarkusTest;
import it.pagopa.oneid.common.model.enums.GrantType;
import it.pagopa.oneid.model.session.enums.ResponseType;
import jakarta.ws.rs.ext.ParamConverter;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class StringToEnumConverterProviderTest {

  private final StringToEnumConverterProvider provider = new StringToEnumConverterProvider();

  @Test
  void testGetConverter_GrantType() {
    ParamConverter<GrantType> converter = provider.getConverter(GrantType.class, null, null);
    assertNotNull(converter);

    // Test fromString with valid value
    assertEquals(GrantType.AUTHORIZATION_CODE, converter.fromString("authorization_code"));

    // Test fromString with invalid value
    assertNull(converter.fromString("invalid_value"));

    // Test toString with non-null value
    assertEquals("AUTHORIZATION_CODE", converter.toString(GrantType.AUTHORIZATION_CODE));

    // Test toString with null value
    assertNull(converter.toString(null));
  }

  @Test
  void testGetConverter_ResponseType() {
    ParamConverter<ResponseType> converter = provider.getConverter(ResponseType.class, null, null);
    assertNotNull(converter);

    // Test fromString with valid value
    assertEquals(ResponseType.CODE, converter.fromString("code"));

    // Test fromString with invalid value
    assertNull(converter.fromString("invalid_value"));

    // Test toString with non-null value
    assertEquals("CODE", converter.toString(ResponseType.CODE));

    // Test toString with null value
    assertNull(converter.toString(null));
  }

  @Test
  void testGetConverter_OtherEnum() {
    // Test with an enum type that is not handled
    ParamConverter<OtherEnum> converter = provider.getConverter(OtherEnum.class, null, null);
    assertNull(converter);
  }

  enum OtherEnum {
    VALUE1, VALUE2;
  }
}
