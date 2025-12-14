package org.itmo.lab1.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Упрощённый валидатор: сейчас мы больше не ходим в БД отсюда,
 * чтобы избежать проблем с обновлением сущностей. Уникальность имени
 * проверяется на уровне сервисов.
 */
public class UniqueOrganizationNameValidator implements ConstraintValidator<UniqueOrganizationName, String> {

    @Override
    public void initialize(UniqueOrganizationName constraintAnnotation) {
        // no-op
    }

    @Override
    public boolean isValid(String name, ConstraintValidatorContext context) {
        // Сам по себе этот валидатор больше ничего не делает, вся логика в сервисе
        return true;
    }
}
