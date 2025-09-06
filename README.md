# 💳 Bank REST API

[![Java](https://img.shields.io/badge/Java-17+-blue?logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.0-brightgreen?logo=springboot)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15.0-blue?logo=postgresql)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker)](https://www.docker.com/)
[![Swagger](https://img.shields.io/badge/docs-Swagger-85EA2D?logo=swagger)](http://localhost:8080/api/swagger-ui/index.html)

RESTful API для управления банковскими картами. Система реализует полный цикл работы с картами: выпуск, пополнение, блокировка, переводы между счетами, а также обеспечивает безопасность через **JWT-аутентификацию** и разграничение прав по ролям (**USER** и **ADMIN**).

---

## 📑 Содержание

- [🚀 Быстрый старт](#-быстрый-старт)
- [🔐 Аутентификация](#-аутентификация)
- [👨‍💻 Роли и доступ](#-роли-и-доступ)
- [📚 Документация API](#-документация-api)
- [🛣️ Эндпоинты API](#-эндпоинты-api)
    - [Auth Controller](#-auth-controller-auth)
    - [User Controller](#-user-controller-users)
    - [Card Controller](#-card-controller-cards)
    - [Test Controller](#-test-controller-apitest)
- [📦 Примеры запросов](#-примеры-запросов)
- [🏗️ Технологический стек](#-технологический-стек)

---

## 🚀 Быстрый старт

Для запуска проекта необходимы **Docker** и **Docker Compose**.

1.  **Клонируйте репозиторий:**
    ```bash
    git clone https://github.com/amirzcheek/Bank_REST.git
    cd Bank_REST
    ```

2.  **Соберите проект и запустите контейнеры:**
    ```bash
    mvn clean package -DskipTests
    docker-compose up --build
    ```

3.  **Готово! Сервисы доступны по адресам:**
    *   Приложение: **http://localhost:8080**
    *   База данных (PostgreSQL): **localhost:5432**
    *   Swagger UI: **http://localhost:8080/api/swagger-ui/index.html**

---

## 🔐 Аутентификация

После запуска в системе автоматически создаются два пользователя:

| Роль | Логин | Пароль |
| :--- | :--- | :--- |
| **ADMIN** | `admin` | `admin123` |
| **USER** | `user` | `user123` |

Для работы с защищенными эндпоинтами необходимо:
1.  Получить JWT-токен через `/auth/login`.
2.  Добавлять этот токен в заголовок каждого последующего запроса:
    ```
    Authorization: Bearer <ваш_JWT_токен>
    ```

---

## 👨‍💻 Роли и доступ

*   **PUBLIC**: Доступ без токена.
*   **USER**: Может управлять своими картами (просмотр, блокировка, переводы).
*   **ADMIN**: Полный доступ ко всем операциям и данным в системе.

---

## 📚 Документация API

Для интерактивного тестирования и просмотра всех эндпоинтов используется **Swagger UI**:
👉 **[http://localhost:8080/api/swagger-ui/index.html](http://localhost:8080/api/swagger-ui/index.html)**

Также вы можете импортировать коллекцию в **Postman**, используя ссылку из Swagger (`/api/v3/api-docs`).

---

## 🛣️ Эндпоинты API

### 🔑 Auth Controller (`/auth`)

| Метод | Эндпоинт | Описание | Тело запроса | Ответ |
| :--- | :--- | :--- | :--- | :--- |
| **POST** | `/auth/register` | Регистрация нового пользователя | `{"username": "string", "password": "string", "role": "USER"}` | JWT + User |
| **POST** | `/auth/login` | Авторизация, получение JWT-токена | `{"username": "string", "password": "string"}` | JWT Token |

### 👤 User Controller (`/users`)

| Метод | Эндпоинт | Доступ | Описание | Ответ |
| :--- | :--- | :--- | :--- | :--- |
| **GET** | `/users` | **ADMIN** | Получить список всех пользователей | `List<UserDto>` |
| **DELETE** | `/users/{id}` | **ADMIN** | Удалить пользователя по ID | `204 No Content` |

### 💳 Card Controller (`/cards`)

| Метод | Эндпоинт | Доступ | Описание | Тело запроса | Ответ |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **GET** | `/cards` | **USER, ADMIN** | Список карт текущего пользователя (пагинация) | — | `Page<CardDto>` |
| **POST** | `/cards` | **ADMIN** | Выпустить новую карту | `{"userId": long, "cardType": "VISA", "initialBalance": 5000}` | `CardDto` |
| **POST** | `/cards/{id}/deposit` | **ADMIN** | Пополнить баланс карты | `{"amount": 1000}` | `CardDto` |
| **POST** | `/cards/{id}/block` | **USER**, ADMIN | Заблокировать карту (USER — только свою) | — | `CardDto` |
| **POST** | `/cards/{id}/activate` | **USER**, ADMIN | Активировать карту (USER — только свою) | — | `CardDto` |
| **POST** | `/cards/transfer` | **USER, ADMIN** | Перевод средств между картами | `{"fromCardId": 1, "toCardId": 2, "amount": 500}` | `String` |
| **GET** | `/cards/{id}/balance` | **USER, ADMIN** | Получить баланс карты | — | `BigDecimal` |
| **GET** | `/cards/all` | **ADMIN** | Получить список всех карт (пагинация) | — | `Page<CardDto>` |
| **DELETE** | `/cards/{id}` | **ADMIN** | Удалить карту | — | `204 No Content` |

### 🧪 Test Controller (`/api/test`)

| Метод | Эндпоинт | Доступ | Описание | Ответ |
| :--- | :--- | :--- | :--- | :--- |
| **GET** | `/api/test` | **PUBLIC** | Тестовый эндпоинт без авторизации | `String` |
| **GET** | `/api/test/auth` | **USER, ADMIN** | Тестовый эндпоинт с авторизацией | `String` |

---

## 📦 Примеры запросов

**1. Авторизация пользователя:**
```bash
curl -X POST "http://localhost:8080/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username": "user", "password": "user123"}'
```
2. Получение списка своих карт (после авторизации):

```bash
curl -X GET "http://localhost:8080/cards?page=0&size=10" \
  -H "Authorization: Bearer <YOUR_JWT_TOKEN>"
```
3. Перевод средств между картами:

```bash
curl -X POST "http://localhost:8080/cards/transfer" \
  -H "Authorization: Bearer <YOUR_JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"fromCardId": 1, "toCardId": 2, "amount": 1000}'
```

🏗️ Технологический стек
Backend: Java 21, Spring Boot 3.4.4, Spring Security, Spring Data JPA, Lombok

База данных: PostgreSQL

Аутентификация: JWT (JSON Web Token)

Документация: Springdoc OpenAPI (Swagger UI)

Контейнеризация: Docker, Docker Compose

Сборка: Maven
