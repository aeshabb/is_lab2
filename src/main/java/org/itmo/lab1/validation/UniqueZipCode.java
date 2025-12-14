package org.itmo.lab1.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = UniqueZipCodeValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UniqueZipCode {
    
    String message() default "Адрес с таким zipCode уже существует";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
    
    /**
     * Игнорировать ID при проверке (для операций UPDATE)
     */
    boolean ignoreId() default false;
}
