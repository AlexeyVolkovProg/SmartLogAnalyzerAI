# SmartLogAnalyzerAI

Исходный код ВКР на тему
`Разработка инструмента диагностики инцидентов в распределённых системах с применением языковых моделей`

## Что делает система

Приложение принимает поток логов от Fluent Bit, сохраняет записи в PostgreSQL
и строит по ним векторные эмбеддинги. По запросу пользователя система выполняет семантический поиск
по накопленным логам и запускает LLM анализ инцидента: модель получает описание проблемы,
сама формирует поисковые запросы, извлекает релевантный контекст из векторного хранилища
и возвращает структурированный отчёт с первопричиной, доказательствами и рекомендациями.

## Технологический стек

| Компонент        | Версия / реализация              |
|------------------|----------------------------------|
| Java             | 21                               |
| Spring Boot      | 4.0                              |
| Spring AI        | 2.0                              |
| LLM              | Ollama `gemma3:4b-it-q4_K_M`   |
| Эмбеддинги       | Ollama `mxbai-embed-large`     |
| База данных      | PostgreSQL 16 + pgvector         |
| Миграции         | Liquibase                        |
| Сборщик логов    | Fluent Bit 3.0                   |
| Frontend         | React 19, TypeScript              |

## Быстрый старт

### Предварительные требования

Установить [Ollama](https://ollama.ai/download) и загрузить модели:

```bash
ollama pull gemma3:4b-it-q4_K_M
ollama pull mxbai-embed-large
```

### Запуск через Docker Compose

```bash
git clone https://github.com/AlexeyVolkovProg/SmartLogAnalyzerAI.git
cd SmartLogAnalyzerAI

docker compose up -d --build
```

После запуска:

| Сервис      | URL                                        |
|-------------|--------------------------------------------|
| Frontend    | http://localhost:3000                      |
| Backend API | http://localhost:8080                      |
| Swagger UI  | http://localhost:8080/swagger-ui.html      |

На первом запуске Liquibase автоматически создаёт схему БД — это занимает около минуты.

### Запуск для разработки (без Docker)

```bash
# Запустить только PostgreSQL
docker compose up -d postgres

# Бэкенд (Windows)
mvnw.cmd spring-boot:run

# Бэкенд (Linux / macOS)
./mvnw spring-boot:run

# Фронтенд
cd frontend
npm install
npm run dev
```

### Развёртывание в Kubernetes

Манифесты находятся в `infra/k8s/`. Перед применением собрать образы и загрузить в кластер:

```bash
docker build -t smartloganalyzerai-app:latest .
docker build -t smartloganalyzerai-frontend:latest ./frontend

# minikube
minikube image load smartloganalyzerai-app:latest
minikube image load smartloganalyzerai-frontend:latest

# Применить все манифесты
kubectl apply -f infra/k8s/
```

Сервисы доступны через NodePort: Frontend — `30300`, Backend — `30080`.

По умолчанию Ollama разворачивается внутри кластера на CPU. Для работы с GPU или внешним Ollama
укажите адрес в `infra/k8s/configmap.yaml` (`SPRING_AI_OLLAMA_BASE_URL`).

## API

Полная документация — Swagger UI: `http://localhost:8080/swagger-ui.html`

**Приём логов:**
```bash
POST /api/logs
```

**Семантический поиск:**
```bash
GET /api/logs/search?query=connection+refused&serviceName=hadoop&limit=10
```

**Анализ инцидента:**
```bash
curl -X POST http://localhost:8080/api/incidents/analyze \
  -H 'Content-Type: application/json' \
  -d '{"description": "Hadoop DataNode перестал отвечать", "mode": "INCIDENT_DESCRIPTION", "maxResults": 20}'
```
**Пример анализа инцидента**

<img width="992" height="642" alt="image" src="https://github.com/user-attachments/assets/c82c7dd3-9bb5-477d-8ed4-e34f527f428a" />

## Архитектура

Система реализует RAG-паттерн: входящие логи векторизуются и индексируются в pgvector;
при анализе инцидента `EnrichSemanticQueryAdvisor` использует LLM для генерации поисковых запросов,
извлекает ближайшие по смыслу записи (HNSW, косинусное расстояние, 1024 измерения)
и передаёт их в контекст финального LLM-запроса.

<img width="974" height="436" alt="image" src="https://github.com/user-attachments/assets/6bf50ee7-45bf-4eb6-a79a-88c09ae738e4" />

## Sequence диаграмма процесса анализа инцидентов

<img width="1016" height="577" alt="image" src="https://github.com/user-attachments/assets/fa393270-83b5-431c-96e6-606fcbc642a5" />

### Структура пакетов

```
com.assistant.smartloganalyzerai/
├── advisor/       RAG обогащение промпта
├── config/        pgvector, Ollama, Liquibase
├── controller/    REST
├── dto/           DTO запросов/ответов
├── entity/        JPA-сущности
├── repository/    Spring Data JPA репозиторий
└── service/       LogIngestionService, LogVectorService, IncidentAnalysisService
```


### Сбор логов

Fluent Bit читает файлы из `logs-datasets/` (Hadoop, Linux), применяет Lua-фильтры
для определения уровня важности и имени сервиса, после чего отправляет батчи на `POST /api/logs`.
Конфигурация находится в `fluentbit-configs/`.
