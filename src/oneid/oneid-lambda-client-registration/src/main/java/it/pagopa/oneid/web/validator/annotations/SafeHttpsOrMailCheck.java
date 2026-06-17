package it.pagopa.oneid.web.validator.annotations;

import it.pagopa.oneid.web.validator.SafeHttpsOrMailValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = SafeHttpsOrMailValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface SafeHttpsOrMailCheck {

  String message() default "Invalid URI or email";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}