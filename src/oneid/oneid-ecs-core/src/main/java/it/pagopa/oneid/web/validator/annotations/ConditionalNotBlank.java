package it.pagopa.oneid.web.validator.annotations;

import it.pagopa.oneid.web.validator.ConditionalNotBlankValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ConditionalNotBlankValidator.class)
public @interface ConditionalNotBlank {

  String message() default "Field must not be blank if the environment is not dev";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  String value() default "";

  String field();
}
