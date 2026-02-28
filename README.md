## Архитектура

| Сервис | Порт | Описание |
|--------|------|----------|
| Auth Server | 9000 | OAuth 2.0 Authorization Server |
| Accounts Service | 8081 | Микросервис аккаунтов |
| Cash Service | 8082 | Микросервис пополнения/снятия |
| Transfer Service | 8083 | Микросервис переводов |
| Notifications Service | 8084 | Микросервис уведомлений |
| Front UI | 8080 | Веб-интерфейс (Thymeleaf) |

Service Discovery, Gateway и Config реализованы средствами Kubernetes (Services, Ingress, ConfigMaps/Secrets).

Базы данных (accounts-db, notifications-db) развёрнуты как StatefulSets.

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

## Развёртывание в Kubernetes

```bash
# Запуск кластера
minikube start --memory=8192 --cpus=4
minikube addons enable ingress
eval $(minikube docker-env)

# Сборка образов
mvn clean package -DskipTests
for svc in auth-server accounts-service cash-service transfer-service notifications-service front-ui; do
  docker build -t bank/$svc:latest $svc/
done

# Деплой зонтичным чартом
helm dependency update helm/
helm upgrade --install bank helm/ -n dev --create-namespace -f helm/values-dev.yaml

# Деплой отдельного сабчарта
helm upgrade --install accounts-service helm/charts/accounts-service -n dev --create-namespace
```

### Среды

| Namespace | Values файл | Реплики | LOG_LEVEL |
|-----------|-------------|---------|-----------|
| dev | values-dev.yaml | 1 | DEBUG |
| test | values-test.yaml | 1 | INFO |
| prod | values-prod.yaml | 2 | WARN |

### Ingress

```bash
echo "$(minikube ip) bank-dev.local" | sudo tee -a /etc/hosts
```

### Helm-тесты

```bash
helm test bank -n dev
```

## CI/CD (Jenkins)

Jenkinsfile для каждого сервиса (`<service>/Jenkinsfile`) и зонтичный (`Jenkinsfile`).

Этапы: Validate → Build → Test → Docker Build & Push → Deploy to Test → Deploy to Prod.

Деплой в prod требует ручного подтверждения.

### Настройка

1. Плагины: Pipeline, Docker Pipeline, Kubernetes CLI
2. Credentials: `docker-registry-url`, kubeconfig
3. Pipeline Job → Script Path: `Jenkinsfile` (или `accounts-service/Jenkinsfile`)
