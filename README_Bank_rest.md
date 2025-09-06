# Система управления банковскими картами

## 📋 Описание
REST API для управления банковскими картами с аутентификацией JWT и ролевым доступом.

## 🛠 Технологии
- Java 21
- Spring Boot 3.2.0
- Spring Security + JWT
- PostgreSQL
- Liquibase
- Docker
- OpenAPI 3.0

## 🚀 Запуск приложения

### Способ 1: Docker Compose (рекомендуется)

# Сборка и запуск
mvn clean package
docker-compose up --build

# Остановка
docker-compose down

### Способ 2: Локальный запуск
# Убедитесь, что PostgreSQL запущен на localhost:5432
mvn spring-boot:run

📊 База данных
Приложение использует PostgreSQL. Миграции управляются через Liquibase.

🔐 Аутентификация
Регистрация: POST /api/auth/register

Логин: POST /api/auth/login

Используйте JWT токен в заголовке Authorization: Bearer <token>

📚 API Документация
После запуска приложения документация доступна по адресу:

Swagger UI: http://localhost:8080/api/swagger-ui.html

OpenAPI: http://localhost:8080/api/api-docs

👥 Роли пользователей
ADMIN: Полный доступ ко всем операциям

USER: Доступ только к своим картам и операциям