package it.pagopa.oneid.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import it.pagopa.oneid.common.model.Client;
import it.pagopa.oneid.common.model.exception.enums.ErrorCode;
import it.pagopa.oneid.exception.CieAgePolicyException;
import it.pagopa.oneid.exception.CieAgePolicyException.Reason;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.schema.XSAny;
import org.opensaml.core.xml.schema.XSString;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.Response;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

class CieAgePolicyServiceTest {

    private static final String CIE_ENTITY_ID = "https://idserver.servizicie.interno.gov.it/idp/profile/SAML2/POST/SSO";
    private static final String CLIENT_ID = "cie-age-policy-client";
    private static final String CALLBACK_URI = "https://client.example/callback?source=cie%2Flogin";
    private static final String STATE = "state with spaces & symbols=+%";
    private static final Clock CLOCK = Clock.fixed(
            Instant.parse("2026-10-28T12:00:00Z"), ZoneOffset.UTC);

    private final CieAgePolicyService service = new CieAgePolicyService(CLOCK, CIE_ENTITY_ID);

    @Test
    @DisplayName("An absent opt-in skips even invalid configuration without reading the clock")
    void given_absent_opt_in_when_validating_then_skip_response_and_clock() {
        Client client = new Client();
        Clock unreadClock = mock(Clock.class);
        CieAgePolicyService skippedService = new CieAgePolicyService(unreadClock, CIE_ENTITY_ID);

        assertDoesNotThrow(() -> skippedService.validate(null, client, CIE_ENTITY_ID,
                CALLBACK_URI, STATE));

        verifyNoInteractions(unreadClock);
    }

    @Test
    @DisplayName("An explicit false opt-in skips without inspecting the response or clock")
    void given_false_opt_in_when_validating_then_skip_response_and_clock() {
        Client client = client(5, 17, 14);
        client.setSpidMinors(false);
        Clock unreadClock = mock(Clock.class);
        CieAgePolicyService skippedService = new CieAgePolicyService(unreadClock, CIE_ENTITY_ID);

        assertDoesNotThrow(() -> skippedService.validate(null, client, CIE_ENTITY_ID,
                CALLBACK_URI, STATE));

        verifyNoInteractions(unreadClock);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = { "https://spid.example/idp", "https://eidas.example/idp" })
    @DisplayName("Non-CIE providers skip opted-in clients without reading the clock")
    void given_non_cie_provider_when_validating_then_skip_response_and_clock(String idp) {
        Clock unreadClock = mock(Clock.class);
        CieAgePolicyService skippedService = new CieAgePolicyService(unreadClock, CIE_ENTITY_ID);

        assertDoesNotThrow(() -> skippedService.validate(null, client(null, -1, -1), idp,
                CALLBACK_URI, STATE));

        verifyNoInteractions(unreadClock);
    }

    @Test
    @DisplayName("CIE provider matching is case insensitive and enforces opt-in")
    void given_uppercase_cie_and_opt_in_when_validating_then_enforce_policy() {
        CieAgePolicyException exception = assertThrows(CieAgePolicyException.class,
                () -> service.validate(response(stringValue("2022-10-28")), client(5, 17, 0),
                        CIE_ENTITY_ID.toUpperCase(Locale.ROOT), CALLBACK_URI, STATE));

        assertFailure(exception, Reason.BELOW_MINIMUM_AGE);
    }

    @ParameterizedTest
    @CsvSource({
            "2021-10-28, 5, 17", "2009-10-28, 5, 17", "2008-10-29, 5, 17",
            "2009-10-28, 17, 17"
    })
    @DisplayName("Both configured age limits are inclusive")
    void given_age_at_bound_when_validating_then_allow(String birthDate, int minAge, int maxAge) {
        assertDoesNotThrow(() -> validate(response(stringValue(birthDate)), client(minAge, maxAge, 0)));
    }

    @ParameterizedTest
    @CsvSource({
            "2021-10-29, 0, BELOW_MINIMUM_AGE",
            "2008-10-28, 0, ABOVE_MAXIMUM_AGE",
            "2010-10-28, 17, PARENTAL_AUTHORIZATION_REQUIRED",
            "2021-10-29, 14, BELOW_MINIMUM_AGE",
            "2008-10-28, 14, ABOVE_MAXIMUM_AGE"
    })
    @DisplayName("Age denials distinguish lower, upper, and parental bounds")
    void given_age_outside_policy_when_validating_then_report_specific_reason(
            String birthDate, int threshold, Reason reason) {
        assertFailure(response(stringValue(birthDate)), client(5, 17, threshold), reason);
    }

    @ParameterizedTest
    @CsvSource(value = { "NULL", "999" }, nullValues = "NULL")
    @DisplayName("A missing maximum and the 999 sentinel impose no upper limit")
    void given_unbounded_maximum_when_validating_then_allow_age_over_999(Integer maxAge) {
        assertDoesNotThrow(() -> validate(response(stringValue("0001-01-01")), client(5, maxAge, 0)));
    }

    @ParameterizedTest
    @CsvSource(value = { "NULL", "0" }, nullValues = "NULL")
    @DisplayName("A missing or zero parental threshold requires no parental authorization")
    void given_no_parental_threshold_when_validating_then_allow_minimum_age(Integer threshold) {
        assertDoesNotThrow(() -> validate(response(stringValue("2021-10-28")),
                client(5, null, threshold)));
    }

    @Test
    @DisplayName("The day before the parental threshold is blocked and its birthday is allowed")
    void given_parental_threshold_when_validating_then_allow_exact_threshold_only() {
        Client client = client(5, 17, 14);

        assertFailure(response(stringValue("2012-10-29")), client,
                Reason.PARENTAL_AUTHORIZATION_REQUIRED);
        assertDoesNotThrow(() -> validate(response(stringValue("2012-10-28")), client));
    }

    @Test
    @DisplayName("A parental threshold above the maximum remains valid configuration")
    void given_parental_threshold_above_maximum_when_validating_then_deny_by_age_not_configuration() {
        assertFailure(response(stringValue("2016-10-28")), client(5, 10, 14),
                Reason.PARENTAL_AUTHORIZATION_REQUIRED);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "NULL, 17, 0", "4, 17, 0", "18, 18, 0", "5, 4, 0", "5, -1, 0",
            "5, 1000, 0", "5, 17, -1", "5, 17, 4", "5, 17, 5", "5, 17, 18",
            "17, 999, 17"
    }, nullValues = "NULL")
    @DisplayName("Invalid configuration is rejected before response parsing or clock access")
    void given_invalid_configuration_when_validating_then_report_unverifiable_without_clock_reads(
            Integer minAge, Integer maxAge, Integer threshold) {
        Clock unreadClock = mock(Clock.class);
        CieAgePolicyService configuredService = new CieAgePolicyService(unreadClock, CIE_ENTITY_ID);

        CieAgePolicyException exception = assertThrows(CieAgePolicyException.class,
                () -> configuredService.validate(null, client(minAge, maxAge, threshold), CIE_ENTITY_ID,
                        CALLBACK_URI, STATE));

        assertFailure(exception, Reason.INVALID_CONFIGURATION);
        verifyNoInteractions(unreadClock);
    }

    @ParameterizedTest
    @CsvSource({ "2020-10-28, 5, 6", "2009-10-28, 16, 17" })
    @DisplayName("Parental thresholds immediately above the minimum and at seventeen are valid")
    void given_valid_parental_threshold_when_validating_then_apply_it(
            String birthDate, int minAge, int threshold) {
        assertDoesNotThrow(() -> validate(response(stringValue(birthDate)), client(minAge, 17, threshold)));
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 2 })
    @DisplayName("The SAML response must contain exactly one assertion")
    void given_wrong_assertion_count_when_validating_then_reject_birth_date(int count) {
        Response response = mock(Response.class);
        List<Assertion> assertions = new ArrayList<>();
        for (int index = 0; index < count; index++) {
            assertions.add(mock(Assertion.class));
        }
        when(response.getAssertions()).thenReturn(assertions);

        assertFailure(response, client(5, 17, 0), Reason.INVALID_BIRTH_DATE);
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 2 })
    @DisplayName("The assertion must contain exactly one attribute statement")
    void given_wrong_statement_count_when_validating_then_reject_birth_date(int count) {
        Response response = mock(Response.class);
        Assertion assertion = mock(Assertion.class);
        List<AttributeStatement> statements = new ArrayList<>();
        for (int index = 0; index < count; index++) {
            statements.add(mock(AttributeStatement.class));
        }
        when(response.getAssertions()).thenReturn(List.of(assertion));
        when(assertion.getAttributeStatements()).thenReturn(statements);

        assertFailure(response, client(5, 17, 0), Reason.INVALID_BIRTH_DATE);
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 2 })
    @DisplayName("Missing or duplicate dateOfBirth attributes are rejected")
    void given_wrong_birth_attribute_count_when_validating_then_reject_birth_date(int count) {
        List<Attribute> attributes = new ArrayList<>();
        for (int index = 0; index < count; index++) {
            attributes.add(attribute("dateOfBirth", List.of(stringValue("2012-10-28"))));
        }

        assertFailure(responseWithAttributes(attributes), client(5, 17, 0), Reason.INVALID_BIRTH_DATE);
    }

    @ParameterizedTest
    @ValueSource(strings = { "DateOfBirth", "dateofbirth", "birthDate" })
    @DisplayName("Only the exact dateOfBirth attribute name is accepted")
    void given_wrong_attribute_name_when_validating_then_reject_birth_date(String name) {
        assertFailure(responseWithAttributes(List.of(attribute(name,
                List.of(stringValue("2012-10-28"))))), client(5, 17, 0), Reason.INVALID_BIRTH_DATE);
    }

    @Test
    @DisplayName("Unrelated attributes do not invalidate a single dateOfBirth")
    void given_unrelated_attributes_when_validating_then_use_only_birth_attribute() {
        Response response = responseWithAttributes(List.of(
                attribute("name", List.of(stringValue("Test"))),
                attribute("dateOfBirth", List.of(stringValue("2012-10-28")))));

        assertDoesNotThrow(() -> validate(response, client(5, 17, 0)));
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 2 })
    @DisplayName("dateOfBirth requires exactly one value even when duplicate values agree")
    void given_wrong_value_count_when_validating_then_reject_birth_date(int count) {
        List<XMLObject> values = new ArrayList<>();
        for (int index = 0; index < count; index++) {
            values.add(stringValue("2012-10-28"));
        }

        assertFailure(responseWithAttributes(List.of(attribute("dateOfBirth", values))),
                client(5, 17, 0), Reason.INVALID_BIRTH_DATE);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {
            "", " ", "\t", "\n", " 2012-10-28", "2012-10-28 ", "28/10/2012",
            "2012-1-01", "2012-01-1", "2012-10-28T00:00:00Z", "2012-10-28Z",
            "2012-02-30", "2011-02-29", "2012-13-01", "2012-00-01", "2012-01-00",
            "2026-10-29", "9999-12-31", "0000-01-01", "-001-01-01", "+2012-10-28"
    })
    @DisplayName("XSString rejects blank, malformed, impossible, future, and year-zero dates")
    void given_invalid_string_birth_date_when_validating_then_report_unverifiable(String birthDate) {
        assertFailure(response(stringValue(birthDate)), client(5, 17, 0), Reason.INVALID_BIRTH_DATE);
    }

    @Test
    @DisplayName("A birth date equal to today is valid but below the configured minimum")
    void given_birth_date_today_when_validating_then_report_below_minimum_not_invalid_date() {
        assertFailure(response(stringValue("2026-10-28")), client(5, 17, 0), Reason.BELOW_MINIMUM_AGE);
    }

    @Test
    @DisplayName("XSAny text content is accepted without requiring a DOM")
    void given_xs_any_birth_date_when_validating_then_allow() {
        XSAny value = mock(XSAny.class);
        when(value.getTextContent()).thenReturn("2012-10-28");
        when(value.getOrderedChildren()).thenReturn(List.of());

        assertDoesNotThrow(() -> validate(response(value), client(5, 17, 0)));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = { "", " ", "2012-02-30", "2012-10-28T00:00:00Z", "0000-01-01" })
    @DisplayName("XSAny values use the same strict birth-date rules as XSString")
    void given_invalid_xs_any_birth_date_when_validating_then_reject(String birthDate) {
        XSAny value = mock(XSAny.class);
        when(value.getTextContent()).thenReturn(birthDate);

        assertFailure(response(value), client(5, 17, 0), Reason.INVALID_BIRTH_DATE);
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    @DisplayName("Nested XML is rejected even when its text forms a valid date")
    void given_nested_xml_when_validating_then_reject_birth_date(boolean stringType) {
        XMLObject value;
        if (stringType) {
            value = stringValue("2012-10-28");
        } else {
            XSAny anyValue = mock(XSAny.class);
            when(anyValue.getTextContent()).thenReturn("2012-10-28");
            value = anyValue;
        }
        when(value.getOrderedChildren()).thenReturn(List.of(mock(XMLObject.class)));

        assertFailure(response(value), client(5, 17, 0), Reason.INVALID_BIRTH_DATE);
    }

    @Test
    @DisplayName("Other XML value types fall back to DOM text content")
    void given_dom_typed_birth_date_when_validating_then_allow() throws Exception {
        assertDoesNotThrow(() -> validate(response(domValue("2012-10-28")), client(5, 17, 0)));
    }

    @ParameterizedTest
    @ValueSource(strings = { "", " ", "2012-02-30", "0000-01-01", "2026-10-29" })
    @DisplayName("DOM fallback does not bypass strict birth-date validation")
    void given_invalid_dom_birth_date_when_validating_then_reject(String birthDate) throws Exception {
        assertFailure(response(domValue(birthDate)), client(5, 17, 0), Reason.INVALID_BIRTH_DATE);
    }

    @Test
    @DisplayName("An unsupported XML value without a DOM cannot supply a birth date")
    void given_value_without_dom_when_validating_then_reject_birth_date() {
        assertFailure(response(mock(XMLObject.class)), client(5, 17, 0), Reason.INVALID_BIRTH_DATE);
    }

    @ParameterizedTest
    @CsvSource({
            "2026-03-28T23:00:00Z, UTC, 2021-03-29",
            "2026-03-29T22:00:00Z, America/Los_Angeles, 2021-03-30",
            "2026-10-24T22:00:00Z, UTC, 2021-10-25",
            "2026-10-25T23:00:00Z, America/Los_Angeles, 2021-10-26"
    })
    @DisplayName("Rome midnight determines birthdays across both DST transitions and clock zones")
    void given_rome_birthday_rollover_when_validating_then_ignore_clock_zone(
            String midnight, String clockZone, String birthDate) {
        Instant rollover = Instant.parse(midnight);
        CieAgePolicyService beforeMidnight = new CieAgePolicyService(
                Clock.fixed(rollover.minusNanos(1), ZoneId.of(clockZone)), CIE_ENTITY_ID);
        CieAgePolicyService atMidnight = new CieAgePolicyService(
                Clock.fixed(rollover, ZoneId.of(clockZone)), CIE_ENTITY_ID);
        Response response = response(stringValue(birthDate));
        Client client = client(5, 17, 0);

        CieAgePolicyException exception = assertThrows(CieAgePolicyException.class,
                () -> beforeMidnight.validate(response, client, CIE_ENTITY_ID, CALLBACK_URI, STATE));
        assertFailure(exception, Reason.BELOW_MINIMUM_AGE);
        assertDoesNotThrow(() -> atMidnight.validate(response, client, CIE_ENTITY_ID, CALLBACK_URI, STATE));
    }

    @ParameterizedTest
    @ValueSource(strings = { "2026-10-28T10:00:00Z", "2026-10-28T21:59:59Z" })
    @DisplayName("A clock whose local date is tomorrow does not move the Rome birthday forward")
    void given_clock_ahead_of_rome_when_validating_then_use_rome_date(String instant) {
        CieAgePolicyService zonedService = new CieAgePolicyService(
                Clock.fixed(Instant.parse(instant), ZoneId.of("Pacific/Kiritimati")), CIE_ENTITY_ID);

        CieAgePolicyException exception = assertThrows(CieAgePolicyException.class,
                () -> zonedService.validate(response(stringValue("2021-10-29")), client(5, 17, 0),
                        CIE_ENTITY_ID, CALLBACK_URI, STATE));

        assertFailure(exception, Reason.BELOW_MINIMUM_AGE);
    }

    @Test
    @DisplayName("A leap-day birth reaches the next age on March first in a non-leap year")
    void given_leap_day_birth_when_validating_non_leap_year_then_use_period_anniversary() {
        Response response = response(stringValue("2020-02-29"));
        Client client = client(5, 17, 0);
        CieAgePolicyService februaryService = new CieAgePolicyService(
                Clock.fixed(Instant.parse("2025-02-28T12:00:00Z"), ZoneOffset.UTC), CIE_ENTITY_ID);
        CieAgePolicyService marchService = new CieAgePolicyService(
                Clock.fixed(Instant.parse("2025-03-01T12:00:00Z"), ZoneOffset.UTC), CIE_ENTITY_ID);

        CieAgePolicyException exception = assertThrows(CieAgePolicyException.class,
                () -> februaryService.validate(response, client, CIE_ENTITY_ID, CALLBACK_URI, STATE));
        assertFailure(exception, Reason.BELOW_MINIMUM_AGE);
        assertDoesNotThrow(() -> marchService.validate(response, client, CIE_ENTITY_ID,
                CALLBACK_URI, STATE));
    }

    @Test
    @DisplayName("A leap-day birthday is reached on February twenty-ninth in a leap year")
    void given_leap_day_birth_when_validating_leap_year_then_allow_on_february_29() {
        CieAgePolicyService leapService = new CieAgePolicyService(
                Clock.fixed(Instant.parse("2024-02-29T12:00:00Z"), ZoneOffset.UTC), CIE_ENTITY_ID);

        assertDoesNotThrow(() -> leapService.validate(response(stringValue("2016-02-29")),
                client(8, 17, 0), CIE_ENTITY_ID, CALLBACK_URI, STATE));
    }

    @Test
    @DisplayName("Null callback and state remain null rather than becoming strings")
    void given_null_callback_context_when_denied_then_preserve_null_values() {
        CieAgePolicyException exception = assertThrows(CieAgePolicyException.class,
                () -> service.validate(response(stringValue("2022-10-28")), client(5, 17, 0),
                        CIE_ENTITY_ID, null, null));

        assertEquals(Reason.BELOW_MINIMUM_AGE, exception.getReason());
        assertEquals(ErrorCode.CIE_AGE_POLICY_DENIED, exception.getErrorCode());
        assertEquals("1004", exception.getErrorCode().getErrorCode());
        assertNull(exception.getRedirectUri());
        assertNull(exception.getState());
        assertEquals(CLIENT_ID, exception.getClientId());
    }

    private Client client(Integer minAge, Integer maxAge, Integer threshold) {
        Client client = new Client();
        client.setClientId(CLIENT_ID);
        client.setSpidMinors(true);
        client.setMinAge(minAge);
        client.setMaxAge(maxAge);
        client.setAgeParentAuth(threshold);
        return client;
    }

    private XSString stringValue(String text) {
        XSString value = mock(XSString.class);
        when(value.getValue()).thenReturn(text);
        return value;
    }

    private XMLObject domValue(String text) throws Exception {
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element element = document.createElementNS("urn:oasis:names:tc:SAML:2.0:assertion", "AttributeValue");
        element.setTextContent(text);
        XMLObject value = mock(XMLObject.class);
        when(value.getDOM()).thenReturn(element);
        return value;
    }

    private Attribute attribute(String name, List<XMLObject> values) {
        Attribute attribute = mock(Attribute.class);
        when(attribute.getName()).thenReturn(name);
        when(attribute.getAttributeValues()).thenReturn(values);
        return attribute;
    }

    private Response response(XMLObject value) {
        return responseWithAttributes(List.of(attribute("dateOfBirth", List.of(value))));
    }

    private Response responseWithAttributes(List<Attribute> attributes) {
        AttributeStatement statement = mock(AttributeStatement.class);
        when(statement.getAttributes()).thenReturn(attributes);
        Assertion assertion = mock(Assertion.class);
        when(assertion.getAttributeStatements()).thenReturn(List.of(statement));
        Response response = mock(Response.class);
        when(response.getAssertions()).thenReturn(List.of(assertion));
        return response;
    }

    private void validate(Response response, Client client) {
        service.validate(response, client, CIE_ENTITY_ID, CALLBACK_URI, STATE);
    }

    private void assertFailure(Response response, Client client, Reason reason) {
        CieAgePolicyException exception = assertThrows(CieAgePolicyException.class,
                () -> validate(response, client));
        assertFailure(exception, reason);
    }

    private void assertFailure(CieAgePolicyException exception, Reason reason) {
        boolean unverifiable = reason == Reason.INVALID_BIRTH_DATE || reason == Reason.INVALID_CONFIGURATION;
        assertEquals(reason, exception.getReason());
        assertEquals(reason.name(), exception.getMessage());
        assertEquals(unverifiable ? ErrorCode.CIE_AGE_POLICY_NOT_VERIFIABLE : ErrorCode.CIE_AGE_POLICY_DENIED,
                exception.getErrorCode());
        assertEquals(unverifiable ? "OI_ERROR" : "1004", exception.getErrorCode().getErrorCode());
        assertEquals(CALLBACK_URI, exception.getRedirectUri());
        assertEquals(STATE, exception.getState());
        assertEquals(CLIENT_ID, exception.getClientId());
    }
}
