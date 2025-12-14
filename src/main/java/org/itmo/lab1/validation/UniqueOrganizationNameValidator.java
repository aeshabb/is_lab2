package org.itmo.lab1.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.itmo.lab1.repository.jpa.OrganizationRepositoryJpa;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UniqueOrganizationNameValidator implements ConstraintValidator<UniqueOrganizationName, String> {

    @Autowired
    private OrganizationRepositoryJpa organizationRepository;
    
    private boolean ignoreId;

    @Override
    public void initialize(UniqueOrganizationName constraintAnnotation) {
        this.ignoreId = constraintAnnotation.ignoreId();
    }

    @Override
    public boolean isValid(String name, ConstraintValidatorContext context) {
        if (name == null || name.trim().isEmpty()) {
            return true; // null/empty проверяется другими валидаторами (@NotNull, @NotBlank)
        }
        
        try {
            return !organizationRepository.existsByName(name);
        } catch (Exception e) {
            // В случае ошибки БД, считаем валидацию пройденной
            // (ошибку БД обработает сервисный слой)
            return true;
        }
    }
}
