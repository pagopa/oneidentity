package it.pagopa.oneid.web.validator.annotations;

import it.pagopa.oneid.web.validator.AuthLevelValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = AuthLevelValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthLevelCheck {

  String message() default "Invalid acr value";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}