# Zipkin — Distributed Tracing

Zipkin используется для сбора и визуализации распределённых трассировок микросервисов.

## Запуск

Zipkin запускается снаружи Kubernetes-кластера через Docker Compose:

```bash
docker-compose up -d zipkin
```

## Доступ

- **UI:** http://localhost:9411
- **API:** http://localhost:9411/api/v2/spans

## Хранилище

Используется in-memory хранилище (по умолчанию). Данные не сохраняются между перезапусками.

## Интеграция с микросервисами

Микросервисы отправляют трейсы в Zipkin через Micrometer Tracing + Brave.
Переменная окружения `ZIPKIN_URL` указывает адрес Zipkin (по умолчанию `http://localhost:9411`).
