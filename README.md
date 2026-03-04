## Архитектура

| Сервис | Порт | Описание |
|--------|------|----------|
| Auth Server | 9000 | OAuth 2.0 Authorization Server |
| Accounts Service | 8081 | Микросервис аккаунтов |
| Cash Service | 8082 | Микросервис пополнения/снятия |
| Transfer Service | 8083 | Микросервис переводов |
| Notifications Service | 8084 | Микросервис уведомлений (Kafka consumer) |
| Front UI | 8080 | Веб-интерфейс (Thymeleaf) |
| Kafka | 9092 | Брокер сообщений (KRaft mode) |
| Zipkin | 9411 | Распределённый трейсинг |
| Prometheus | 9090 | Сбор и хранение метрик |
| Grafana | 3000 | Визуализация метрик и дашборды |
| Elasticsearch | 9200 | Хранение и поиск логов |
| Logstash | 5000 | Приём и обработка логов |
| Kibana | 5601 | Визуализация логов |

Уведомления передаются через Apache Kafka (топик `bank-notifications`).
Accounts, Cash, Transfer — продюсеры. Notifications — консьюмер.

Service Discovery, Gateway и Config реализованы средствами Kubernetes (Services, Ingress, ConfigMaps/Secrets).

Базы данных (accounts-db, notifications-db) развёрнуты как StatefulSets. Kafka — StatefulSet с PVC.

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

## Стек наблюдаемости (Observability)

Компоненты стека наблюдаемости развёрнуты снаружи Kubernetes-кластера (через Docker Compose).
Конфигурации хранятся в подпроектах `zipkin/`, `prometheus/`, `grafana/`, `elk/`.
Для доступа изнутри Kubernetes используются Helm-сабчарты с ExternalName Services.

### Zipkin — распределённый трейсинг

- **UI:** http://localhost:9411
- **Интеграция:** Micrometer Tracing + Brave → трейсы HTTP-запросов (входящих/исходящих), JDBC, Kafka
- **Конфигурация:** переменная `ZIPKIN_URL` в каждом сервисе
- **Хранилище:** in-memory

### Prometheus — метрики

- **UI:** http://localhost:9090
- **Endpoint:** `/actuator/prometheus` на каждом сервисе
- **Scrape interval:** 15s
- **Стандартные метрики:** HTTP RPS, 4xx/5xx, персентили таймингов (P50, P95, P99), JVM (memory, CPU, threads)
- **Конфигурация:** `prometheus/prometheus.yml`

### Кастомные бизнес-метрики

| Метрика | Описание | Теги |
|---------|----------|------|
| `bank_cash_withdraw_failures_total` | Неуспешные попытки снятия денег | `login` |
| `bank_transfer_failures_total` | Неуспешные попытки перевода | `from_login`, `to_login` |
| `bank_notification_failures_total` | Ошибки отправки нотификаций | `login` |

### Алерты (Prometheus)

Правила алертов определены в `prometheus/alert-rules.yml`:
- `HighHttpErrorRate` — высокий уровень 5xx ошибок
- `HighHttpLatency` — P95 latency > 2s
- `HighFailedWithdrawals` — частые неуспешные снятия
- `HighFailedTransfers` — частые неуспешные переводы
- `NotificationSendFailures` — частые ошибки нотификаций
- `ServiceDown` — сервис недоступен

### Grafana — дашборды

- **UI:** http://localhost:3000 (admin/admin)
- **Datasource:** Prometheus (автоматически подключен через provisioning)
- **Дашборды** (автоматически загружены из `grafana/dashboards/`):
  - **HTTP Metrics** — RPS, 4xx, 5xx, P50/P95/P99 по сервисам
  - **JVM Metrics** — Heap/Non-Heap memory, CPU, Threads по сервисам
  - **Business Metrics** — неуспешные снятия, переводы, нотификации

### ELK Stack — логирование

- **Kibana:** http://localhost:5601
- **Формат логов:** JSON (LogstashEncoder), единый для всех сервисов (паттерн Microservice Chassis)
- **Trace/Span ID:** автоматически включены в каждый лог через MDC (Micrometer Tracing)
- **Logstash pipeline:** TCP input (port 5000) → фильтрация (маскировка паролей/номеров) → Elasticsearch
- **Конфигурация:** `elk/logstash/logstash.conf`

### Уровни логирования

| Уровень | Назначение |
|---------|------------|
| `ERROR` | Ошибки (исключения, fallback, невозможность отправки) |
| `WARN` | Нештатные ситуации (недостаточно средств, компенсация) |
| `INFO` | Ключевые бизнес-события (успешный перевод, создание счёта) |
| `DEBUG` | Диагностика (входные параметры, промежуточные результаты) |

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
