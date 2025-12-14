# Инструкция по запуску

## Предварительные требования

1. Java 17+
2. Maven 3.6+
3. PostgreSQL 12+
4. WildFly 27+ (или другой Jakarta EE сервер)
5. Apache JMeter 5.6+ (для тестирования)

## Шаги по развертыванию

### 1. Подготовка базы данных

```sql
-- Создайте базу данных
CREATE DATABASE organizations_db;

-- Создайте пользователя
CREATE USER lab_user WITH PASSWORD 'password';
GRANT ALL PRIVILEGES ON DATABASE organizations_db TO lab_user;
```

**Примечание:** Таблицы создаются автоматически через JPA (hibernate.hbm2ddl.auto=update)

### 2. Настройка DataSource в WildFly

Добавьте DataSource в `standalone.xml`:

```xml
<datasource jndi-name="java:/PostgresDS" pool-name="PostgresDS">
    <connection-url>jdbc:postgresql://localhost:5432/organizations_db</connection-url>
    <driver>postgresql</driver>
    <security>
        <user-name>lab_user</user-name>
        <password>password</password>
    </security>
</datasource>
```

### 3. Сборка проекта

```bash
mvn clean package
```

### 4. Развертывание на WildFly

```bash
# Скопируйте WAR файл в директорию развертывания WildFly
cp target/lab2.war $WILDFLY_HOME/standalone/deployments/

# Или используйте Maven плагин
mvn wildfly:deploy
```

### 5. Доступ к приложению

Откройте браузер и перейдите:
- Главная страница: `http://localhost:8080/lab2/`
- Импорт организаций: `http://localhost:8080/lab2/import`
- Специальные операции: `http://localhost:8080/lab2/special`

## Использование функционала импорта

1. Перейдите на страницу импорта: `http://localhost:8080/lab2/import`
2. Введите имя пользователя
3. Выберите JSON файл (используйте `import-example.json` как шаблон)
4. Нажмите "Загрузить и импортировать"
5. Просмотрите результат в таблице истории импорта

## Запуск JMeter тестов

### GUI режим (для разработки)

```bash
jmeter -t jmeter/organization-load-test.jmx
```

### Консольный режим (для CI/CD)

```bash
jmeter -n -t jmeter/organization-load-test.jmx -l results.jtl -e -o report
```

Отчет будет доступен в папке `report/index.html`

## Проверка работы транзакций

### Тест 1: Проверка уникальности имени организации

1. Создайте организацию с именем "Test Org"
2. Попробуйте создать еще одну организацию с тем же именем
3. Должна появиться ошибка: "Организация с именем 'Test Org' уже существует"

### Тест 2: Проверка уникальности zipCode

1. Создайте организацию с postal address zipCode "1234567"
2. Попробуйте создать другую организацию с postal address zipCode "1234567"
3. Должна появиться ошибка: "Адрес с zipCode '1234567' уже существует"

### Тест 3: Проверка транзакционности импорта

1. Подготовьте JSON файл с несколькими организациями
2. В одной из организаций укажите невалидные данные (например, отрицательный rating)
3. Выполните импорт
4. Убедитесь, что ни одна организация не была создана (откат транзакции)
5. В истории импорта должна быть запись со статусом FAILED

### Тест 4: Одновременное обновление (JMeter)

1. Запустите JMeter тест "Concurrent Update - Same Record"
2. В результатах увидите, что только одна транзакция успешна
3. Остальные получат ошибку из-за конфликта блокировок

## Устранение неполадок

### Ошибка подключения к БД

Проверьте:
- PostgreSQL запущен: `sudo systemctl status postgresql`
- Правильность учетных данных в `standalone.xml`
- Доступность БД: `psql -U lab_user -d organizations_db`

### Ошибка развертывания

Проверьте логи WildFly:
```bash
tail -f $WILDFLY_HOME/standalone/log/server.log
```

### Ошибки при импорте

Проверьте:
- Формат JSON файла соответствует примеру
- Все обязательные поля заполнены
- Значения полей соответствуют ограничениям (например, rating > 0)
- Имена организаций и zipCode уникальны

## Структура файла импорта

```json
[
  {
    "name": "Уникальное имя",
    "coordinates": {
      "x": 100,           // > -331
      "y": 200.5
    },
    "officialAddress": {   // опционально
      "street": "Улица",
      "zipCode": "1234567" // уникальный, мин. 7 символов
    },
    "annualTurnover": 1000000,  // > 0
    "employeesCount": 50,        // > 0
    "rating": 4.5,               // > 0
    "type": "COMMERCIAL",        // COMMERCIAL, PUBLIC, PRIVATE_LIMITED_COMPANY, OPEN_JOINT_STOCK_COMPANY
    "postalAddress": {
      "street": "Улица 2",
      "zipCode": "7654321" // уникальный, мин. 7 символов
    }
  }
]
```

## Дополнительная информация

Подробная документация находится в файле `LAB2_README.md`
