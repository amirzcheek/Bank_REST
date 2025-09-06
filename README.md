🚀 Запуск проекта

Требования:

Docker и Docker Compose установлены.

1. Клонировать репозиторий
   git clone https://github.com/amirzcheek/Bank_REST.git
   cd Bank_REST

2. Запустить приложение
   mvn clean package -DskipTests
   docker-compose up --build


Это поднимет:

PostgreSQL (порт 5432)

Spring Boot приложение (порт 8080)

3. Доступ к API
   🔑 Авторизация

Сразу создаются два пользователя:

Admin:

login: admin

password: admin123

User:

login: user

password: user123

📌 Swagger UI

Документация и тестирование API:
👉 http://localhost:8080/api/swagger-ui/index.html

📌 Postman

Можно использовать Postman:

Отправить запрос на логин:

POST http://localhost:8080/auth/login

{
"username": "user",
"password": "user123"
}


В ответе придёт JWT-токен.

Для всех остальных запросов указывать в Headers:

Authorization: Bearer <ваш_JWT_токен>

📘 API Endpoints
🔑 AuthController (/auth)
Метод	    Endpoint	            Описание	                            Тело запроса	                                                            Ответ
POST	    /auth/register	        Регистрация нового пользователя	        json { "username": "john", "password": "12345", "role": "USER" }	JWT токен + данные пользователя
POST	    /auth/login	            Авторизация (получение токена)	json { "username": "john", "password": "12345" }	JWT токен


👤 UserController (/users)
Метод	    Endpoint	          Доступ            Описание	                                                 Ответ
GET	        /users	              ADMIN	            Получить список всех пользователей	                         List<UserDto>
DELETE	    /users/{id}	          ADMIN	            Удалить пользователя по ID	                                 204 No Content


💳 CardController (/cards)
Метод	    Endpoint	                Доступ            Описание	                                                Тело запроса	                                                            Ответ
GET	        /cards	                USER, ADMIN	          Получить список карт текущего пользователя (с пагинацией)	    —	                                                                Page<CardDto>
POST	    /cards	                    ADMIN	          Создать карту	                                            json { "userId": 1, "cardType": "VISA", "initialBalance": 5000 }	        CardDto
POST	    /cards/{id}/deposit	        ADMIN	          Пополнить карту	                                        json { "amount": 1000 }	                                                    CardDto
POST	    /cards/{id}/block	    USER, ADMIN	          Заблокировать карту (USER может только свою)	                —	                                                                    CardDto
POST	    /cards/{id}/activate	USER, ADMIN	          Активировать карту (USER может только свою)	                —	                                                                    CardDto
POST	    /cards/transfer	        USER, ADMIN	          Перевод между картами	                                    json { "fromCardId": 1, "toCardId": 2, "amount": 500 }	                "Transfer successful"
GET	        /cards/{id}/balance	    USER, ADMIN	          Проверить баланс карты	                                    —	                                                                    BigDecimal
GET	        /cards/all	                ADMIN	          Получить список всех карт	                                    —	                                                                    Page<CardDto>
DELETE	    /cards/{id}	                ADMIN	          Удалить карту	                                                —	                                                                    204 No Content


🧪 TestController (/api/test)
Метод	    Endpoint	        Доступ          Описание	                                                                         Ответ
GET	        /api/test	                        Public	Тестовый эндпоинт без авторизации	                                    "Hello World!"
GET	        /api/test/auth	    USER, ADMIN	    Тестовый эндпоинт с JWT авторизацией	                                        "Hello Authenticated User!"


⚙️ Примечания

Для всех защищённых эндпоинтов (USER, ADMIN) требуется JWT-токен.
Его можно получить через POST /auth/login и передавать в Authorization: Bearer <token>.


Пример перевода
curl -X POST http://localhost:8080/api/cards/transfer \
-H "Authorization: Bearer <jwt_token>" \
-H "Content-Type: application/json" \
-d '{"fromCardId":1,"toCardId":2,"amount":1000}'
