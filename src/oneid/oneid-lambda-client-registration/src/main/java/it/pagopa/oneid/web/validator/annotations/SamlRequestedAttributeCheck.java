package it.pagopa.oneid.web.validator.annotations;

import it.pagopa.oneid.web.validator.SamlRequestedAttributeValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = SamlRequestedAttributeValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SamlRequestedAttributeCheck {

  String message() default "Invalid Identifier value";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}