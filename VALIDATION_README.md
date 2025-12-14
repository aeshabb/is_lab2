# Валидация уникальности с помощью Bean Validation

## Описание архитектуры

Проект использует **кастомные Bean Validation аннотации** для проверки уникальности на уровне приложения (без ограничений в БД).

## Структура валидаторов

### 1. Кастомные аннотации

#### `@UniqueOrganizationName`
```java
@UniqueOrganizationName
private String name;
```
Проверяет, что организация с таким именем еще не существует в системе.

#### `@UniqueZipCode`
```java
@UniqueZipCode
private String zipCode;
```
Проверяет, что адрес с таким почтовым индексом еще не существует в системе.

### 2. Реализация валидаторов

#### `UniqueOrganizationNameValidator`
```java
@Component
public class UniqueOrganizationNameValidator 
    implements ConstraintValidator<UniqueOrganizationName, String> {
    
    @Autowired
    private OrganizationRepositoryJpa organizationRepository;
    
    @Override
    public boolean isValid(String name, ConstraintValidatorContext context) {
        if (name == null || name.trim().isEmpty()) {
            return true; // Проверяется другими аннотациями
        }
        return !organizationRepository.existsByName(name);
    }
}
```

#### `UniqueZipCodeValidator`
```java
@Component
public class UniqueZipCodeValidator 
    implements ConstraintValidator<UniqueZipCode, String> {
    
    @Autowired
    private AddressRepositoryJpa addressRepository;
    
    @Override
    public boolean isValid(String zipCode, ConstraintValidatorContext context) {
        if (zipCode == null || zipCode.trim().isEmpty()) {
            return true; // Проверяется другими аннотациями
        }
        return !addressRepository.existsByZipCode(zipCode);
    }
}
```

## Использование

### В моделях

**Organization.java:**
```java
@Entity
public class Organization {
    @UniqueOrganizationName
    @NotNull
    @NotBlank
    private String name;
    // ...
}
```

**Address.java:**
```java
@Entity
public class Address {
    @UniqueZipCode
    @NotNull
    @Size(min = 7)
    private String zipCode;
    // ...
}
```

### В DTO

**OrganizationImportDto.java:**
```java
public class OrganizationImportDto {
    @UniqueOrganizationName
    @NotNull
    @NotBlank
    private String name;
    
    private AddressDto postalAddress;
    
    public static class AddressDto {
        @UniqueZipCode
        @NotNull
        @Size(min = 7)
        private String zipCode;
        // ...
    }
}
```

### В контроллерах

```java
@PostMapping("/create")
public String createOrganization(@Valid @ModelAttribute Organization organization,
                                BindingResult result) {
    if (result.hasErrors()) {
        // Обработка ошибок валидации
        return "form";
    }
    organizationService.createOrganization(organization);
    return "redirect:/";
}
```

## Преимущества подхода

1. ✅ **Декларативность** - валидация объявляется на уровне аннотаций
2. ✅ **Переиспользуемость** - аннотации можно применять к любым полям
3. ✅ **Автоматическая проверка** - Spring автоматически вызывает валидаторы при использовании `@Valid`
4. ✅ **Без дублирования кода** - нет необходимости в ручных проверках в сервисах
5. ✅ **Централизованная логика** - все проверки уникальности в одном месте
6. ✅ **Гибкость** - легко добавить дополнительные параметры (например, ignoreId для UPDATE)

## Порядок работы

1. Контроллер получает объект с аннотацией `@Valid`
2. Spring вызывает все валидаторы для полей объекта
3. Кастомные валидаторы обращаются к репозиториям для проверки уникальности
4. Если валидация не пройдена, объект `BindingResult` содержит ошибки
5. Контроллер обрабатывает ошибки или передает объект в сервис

## Создание таблиц

Таблицы создаются **автоматически** через JPA/Hibernate:

**persistence.xml:**
```xml
<property name="hibernate.hbm2ddl.auto" value="update"/>
```

При запуске приложения Hibernate:
1. Сканирует все классы с аннотацией `@Entity`
2. Создает таблицы, если их нет
3. Обновляет структуру при изменении моделей

## Транзакции

Валидация работает **до начала транзакции**, что позволяет:
- Быстро отклонить невалидные данные
- Не загружать БД лишними запросами
- Обеспечить консистентность на уровне приложения

Для импорта используется уровень изоляции **SERIALIZABLE**:
```java
@Transactional(isolation = Isolation.SERIALIZABLE, rollbackFor = Exception.class)
public ImportHistory importOrganizations(List<OrganizationImportDto> dtos, String username) {
    // Валидация каждого DTO через validator.validate()
    // Проверка уникальности внутри файла
    // Создание организаций
}
```

## Обработка ошибок

### В контроллере:
```java
if (result.hasErrors()) {
    result.getAllErrors().forEach(error -> {
        // error.getDefaultMessage() содержит текст ошибки
    });
    return "form";
}
```

### В ImportService:
```java
Set<ConstraintViolation<OrganizationImportDto>> violations = validator.validate(dto);
if (!violations.isEmpty()) {
    for (ConstraintViolation<OrganizationImportDto> violation : violations) {
        validationErrors.add("Организация #" + (i + 1) + ": " + violation.getMessage());
    }
}
```

## Файловая структура

```
src/main/java/org/itmo/lab2/
├── validation/
│   ├── UniqueOrganizationName.java       # Аннотация
│   ├── UniqueOrganizationNameValidator.java  # Валидатор
│   ├── UniqueZipCode.java                # Аннотация
│   └── UniqueZipCodeValidator.java       # Валидатор
├── model/
│   ├── Organization.java                 # @UniqueOrganizationName на поле name
│   ├── Address.java                      # @UniqueZipCode на поле zipCode
│   └── ImportHistory.java
├── dto/
│   └── OrganizationImportDto.java        # Аннотации на полях DTO
└── ...
```

## Примеры использования

### Создание организации через веб-форму:
1. Пользователь заполняет форму
2. Spring вызывает валидаторы автоматически (`@Valid`)
3. Если имя не уникально, показывается ошибка "Организация с таким именем уже существует"
4. Объект не попадает в сервисный слой

### Импорт из JSON:
1. ImportService вызывает `validator.validate(dto)` для каждой организации
2. Все ошибки собираются в список
3. Если есть ошибки, транзакция откатывается
4. История сохраняется со статусом FAILED и сообщением об ошибках

### Одновременное создание:
1. Два пользователя пытаются создать организации с одинаковым именем
2. Первый валидатор проходит успешно (имени еще нет в БД)
3. Второй валидатор также проходит (транзакция первого еще не зафиксирована)
4. На уровне REPEATABLE_READ одна из транзакций откатится с ошибкой сериализации
5. Пользователь может повторить попытку, и валидатор покажет ошибку уникальности
