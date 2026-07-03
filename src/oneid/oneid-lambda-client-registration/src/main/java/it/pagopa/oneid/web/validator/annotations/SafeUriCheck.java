package it.pagopa.oneid.web.validator.annotations;

import it.pagopa.oneid.web.validator.SafeUriValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = SafeUriValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface SafeUriCheck {

  String message() default "Invalid URI";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}