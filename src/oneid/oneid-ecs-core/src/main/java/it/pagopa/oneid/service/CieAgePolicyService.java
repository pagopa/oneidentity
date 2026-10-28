package it.pagopa.oneid.service;

import it.pagopa.oneid.common.model.Client;
import it.pagopa.oneid.exception.CieAgePolicyException;
import it.pagopa.oneid.exception.CieAgePolicyException.Reason;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.Clock;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.List;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.schema.XSAny;
import org.opensaml.core.xml.schema.XSString;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.Response;
import org.w3c.dom.Element;

@ApplicationScoped
public class CieAgePolicyService {

    private static final ZoneId POLICY_ZONE = ZoneId.of("Europe/Rome");
    private final Clock clock;
    private final String cieEntityId;

    @Inject
    public CieAgePolicyService(Clock clock, @ConfigProperty(name = "cie_entity_id") String cieEntityId) {
        this.clock = clock;
        this.cieEntityId = cieEntityId;
    }

    public void validate(Response response, Client client, String idp, String redirectUri, String state) {
        if (!client.isSpidMinors() || !cieEntityId.equalsIgnoreCase(idp)) {
            return;
        }

        Integer minAge = client.getMinAge();
        int maxAge = client.getMaxAge() == null ? 999 : client.getMaxAge();
        int ageParentAuth = client.getAgeParentAuth() == null ? 0 : client.getAgeParentAuth();
        if (minAge == null || minAge < 5 || minAge > 17 || maxAge < minAge || maxAge > 999
                || (ageParentAuth != 0 && (ageParentAuth <= minAge || ageParentAuth >= 18))) {
            throw failure(Reason.INVALID_CONFIGURATION, client, redirectUri, state);
        }

        LocalDate today = LocalDate.now(clock.withZone(POLICY_ZONE));
        LocalDate birthDate = birthDate(response, client, redirectUri, state);
        if (birthDate.isAfter(today)) {
            throw failure(Reason.INVALID_BIRTH_DATE, client, redirectUri, state);
        }

        int age = Period.between(birthDate, today).getYears();
        if (age < minAge) {
            throw failure(Reason.BELOW_MINIMUM_AGE, client, redirectUri, state);
        }
        if (maxAge != 999 && age > maxAge) {
            throw failure(Reason.ABOVE_MAXIMUM_AGE, client, redirectUri, state);
        }
        if (ageParentAuth > 0 && age < ageParentAuth) {
            throw failure(Reason.PARENTAL_AUTHORIZATION_REQUIRED, client, redirectUri, state);
        }
    }

    private LocalDate birthDate(Response response, Client client, String redirectUri, String state) {
        if (response.getAssertions().size() != 1
                || response.getAssertions().getFirst().getAttributeStatements().size() != 1) {
            throw failure(Reason.INVALID_BIRTH_DATE, client, redirectUri, state);
        }
        List<Attribute> birthAttributes = response.getAssertions().getFirst().getAttributeStatements()
                .getFirst().getAttributes().stream()
                .filter(attribute -> "dateOfBirth".equals(attribute.getName()))
                .toList();
        if (birthAttributes.size() != 1 || birthAttributes.getFirst().getAttributeValues().size() != 1) {
            throw failure(Reason.INVALID_BIRTH_DATE, client, redirectUri, state);
        }

        XMLObject value = birthAttributes.getFirst().getAttributeValues().getFirst();
        List<XMLObject> children = value.getOrderedChildren();
        if (children != null && !children.isEmpty()) {
            throw failure(Reason.INVALID_BIRTH_DATE, client, redirectUri, state);
        }
        String text;
        if (value instanceof XSString stringValue) {
            text = stringValue.getValue();
        } else if (value instanceof XSAny anyValue) {
            text = anyValue.getTextContent();
        } else {
            Element element = value.getDOM();
            text = element == null ? null : element.getTextContent();
        }
        if (text == null || !text.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")) {
            throw failure(Reason.INVALID_BIRTH_DATE, client, redirectUri, state);
        }
        try {
            LocalDate birthDate = LocalDate.parse(text);
            if (birthDate.getYear() == 0) {
                throw failure(Reason.INVALID_BIRTH_DATE, client, redirectUri, state);
            }
            return birthDate;
        } catch (DateTimeParseException exception) {
            throw failure(Reason.INVALID_BIRTH_DATE, client, redirectUri, state);
        }
    }

    private CieAgePolicyException failure(Reason reason, Client client, String redirectUri,
            String state) {
        return new CieAgePolicyException(reason, redirectUri, state, client.getClientId());
    }
}
