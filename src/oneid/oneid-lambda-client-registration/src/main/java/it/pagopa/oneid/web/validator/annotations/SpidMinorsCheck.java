package it.pagopa.oneid.web.validator.annotations;

import it.pagopa.oneid.web.validator.SpidMinorsValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = SpidMinorsValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface SpidMinorsCheck {

  String message() default "Invalid SPID minors configuration";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
