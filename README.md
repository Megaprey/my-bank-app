## Пользователи для входа

| Логин | Пароль |
|-------|--------|
| ivanov | password |
| petrov | password |

## Сборка проекта

```bash
mvn clean package -DskipTests
```

## Запуск в Docker

```bash
mvn clean package -DskipTests
docker compose up --build
```

Приложение будет доступно по адресу http://localhost:8080.
