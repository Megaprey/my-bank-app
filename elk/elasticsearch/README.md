# Elasticsearch

Elasticsearch используется как хранилище логов для ELK-стека.

## Конфигурация

Используется конфигурация по умолчанию с параметрами single-node кластера.
Безопасность xpack отключена для упрощения разработки.

## Запуск

```bash
docker-compose up -d elasticsearch
```

## Доступ

- **API:** http://localhost:9200
