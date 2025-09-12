package it.pagopa.oneid.web.validator.annotations;

import it.pagopa.oneid.web.validator.LocalizedContentMapValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = LocalizedContentMapValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface LocalizedContentMapCheck {

  String message() default "Invalid localized content map";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}