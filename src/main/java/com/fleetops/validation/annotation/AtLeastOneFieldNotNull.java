// src/main/java/com/fleetops/validation/AtLeastOneFieldNotNull.java
package com.fleetops.validation.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = com.fleetops.validation.AtLeastOneFieldNotNullValidator.class)
@Documented
public @interface AtLeastOneFieldNotNull {

    String message() default "At least one field must be non-null";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
