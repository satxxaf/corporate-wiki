# corporate-wiki
Корпоративная Wiki - система хранения и версионирования статей

Бэкенд системы «Корпоративная Wiki»: Kotlin 1.9.22, Spring Boot 3.3.5, JDK 21, PostgreSQL 16.

## Запуск

```bash
docker-compose up --build
```

- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- Параметры БД и `JWT_SECRET` читаются из `.env` (шаблон — `.env.example`).

Локальный запуск без Docker (PostgreSQL с БД `corporate_wiki` на localhost:5432):
задайте переменные окружения `JWT_SECRET` (не менее 32 байт), `DB_USER`, `DB_PASSWORD` и выполните `gradle bootRun`.

## Проверка

```bash
# вход
curl -s -X POST localhost:8080/api/auth/login -H 'Content-Type: application/json' \
     -d '{"login":"admin","password":"admin"}'

# список статей (подставьте токен)
curl -s localhost:8080/api/articles -H "Authorization: Bearer <TOKEN>"

# поиск по тегам (статьи со всеми тегами)
curl -s "localhost:8080/api/articles?tags=docker,spring" -H "Authorization: Bearer <TOKEN>"

# откат статьи 1 к версии 1
curl -s -X POST localhost:8080/api/articles/1/versions/1/restore -H "Authorization: Bearer <TOKEN>"
```

## Эндпоинты

| Метод | Путь | Доступ |
|---|---|---|
| POST | /api/auth/register, /api/auth/login | публично |
| GET | /api/auth/me | авторизованный |
| POST | /api/articles | ADMIN, EDITOR |
| GET | /api/articles | авторизованный |
| GET | /api/articles/{id} | авторизованный |
| PUT | /api/articles/{id} | ADMIN, EDITOR |
| DELETE | /api/articles/{id} | автор или ADMIN |
| PUT | /api/articles/{id}/tags | ADMIN, EDITOR |
| GET | /api/articles/{id}/versions | авторизованный |
| GET | /api/articles/{id}/versions/{no} | авторизованный |
| POST | /api/articles/{id}/versions/{no}/restore | ADMIN, EDITOR |
| GET | /api/categories | авторизованный |
| GET | /api/tags | авторизованный |
| PUT | /api/users/{id}/roles | ADMIN |
| POST | /api/admin/articles/{id}/undelete | ADMIN |

Коды ошибок: 400 (валидация), 401 (нет или недействителен токен), 403 (нет прав), 404, 409 (конфликт версий `rowVersion`, дубликаты).