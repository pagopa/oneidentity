package it.pagopa.oneid.web.validator.annotations;

import it.pagopa.oneid.web.validator.SafeRedirectUrisValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = SafeRedirectUrisValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface SafeRedirectUrisCheck {

  String message() default "Invalid redirect URI";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
