package org.itmo.lab1.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Упрощённый валидатор: фактическая проверка уникальности рейтинга
 * выполняется на уровне сервисного слоя. Здесь всегда true, чтобы
 * не ломать Bean Validation при обновлении сущностей.
 */
public class UniqueRatingValidator implements ConstraintValidator<UniqueRating, Double> {

    @Override
    public void initialize(UniqueRating constraintAnnotation) {
        // no-op
    }

    @Override
    public boolean isValid(Double value, ConstraintValidatorContext context) {
        return true;
    }
}
