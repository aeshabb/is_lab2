package org.itmo.lab1.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.itmo.lab1.repository.jpa.AddressRepositoryJpa;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UniqueZipCodeValidator implements ConstraintValidator<UniqueZipCode, String> {

    @Autowired
    private AddressRepositoryJpa addressRepository;
    
    private boolean ignoreId;

    @Override
    public void initialize(UniqueZipCode constraintAnnotation) {
        this.ignoreId = constraintAnnotation.ignoreId();
    }

    @Override
    public boolean isValid(String zipCode, ConstraintValidatorContext context) {
        if (zipCode == null || zipCode.trim().isEmpty()) {
            return true; // null/empty проверяется другими валидаторами (@NotNull, @Size)
        }
        
        try {
            return !addressRepository.existsByZipCode(zipCode);
        } catch (Exception e) {
            // В случае ошибки БД, считаем валидацию пройденной
            // (ошибку БД обработает сервисный слой)
            return true;
        }
    }
}
