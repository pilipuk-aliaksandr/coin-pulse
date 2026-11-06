# 📡 CoinPulse

> Сервис для отслеживания и анализа исторических цен криптовалют с Binance

Сервис автоматически собирает средние цены торговых пар с биржи Binance, сохраняет их в PostgreSQL и предоставляет доступ через REST API. 

Поддерживает расчёт средней цены за период, пагинированный просмотр записей и определение тренда.

---

## 🚀 Быстрый старт

### Требования

- **Docker** и **Docker Compose** (v2)

### Запуск одной командой

```bash
docker compose up --build
```

Это поднимет всё необходимое: PostgreSQL, применит миграции БД и запустит приложение.

> Приложение будет доступно на `http://localhost:8080`
> Swagger UI: `http://localhost:8080/swagger-ui.html`

### Пошаговый запуск

```bash
# 1. Собрать образ и поднять все сервисы
docker compose up --build

# 2. Проверить статус
docker compose ps

# 3. Посмотреть логи приложения
docker compose logs -f app

# 4. Остановить всё
docker compose down

# 5. Остановить + удалить volumes (сброс БД)
docker compose down -v
```

### Локальная разработка (без Docker)

```bash
# Запустить PostgreSQL отдельно
docker compose up postgres liquibase

# В другом терминале — приложение
./gradlew bootRun
```

---

## ⚙️ Переменные окружения

| Переменная                   | Описание                                    |
|------------------------------|---------------------------------------------|
| `BINANCE_POLL_INTERVAL`      | Интервал опроса Binance (30 секунд) |
| `BINANCE_BASE_URL`           | URL Binance API                             |
| `BINANCE_SYMBOLS`            | Список торговых пар через запятую            |
| `SPRING_DATASOURCE_URL`      | URL подключения к PostgreSQL                |
| `SPRING_DATASOURCE_USERNAME` | Имя пользователя БД                         |
| `SPRING_DATASOURCE_PASSWORD` | Пароль БД                                   |

---

## 🔧 Что сделано сверх ТЗ

| # | Возможность | Зачем это |
|---|-------------|-----------|
| 1 | **Полная контейнеризация приложения** | Dockerfile с multi-stage build — приложение запускается одной командой `docker compose up --build` без локальных зависимостей |
| 2 | **Поддержка нескольких торговых пар** | BNBUSDT, BTCUSDT, ETHUSDT вместо одной пары по ТЗ |
| 3 | **Contract-first подход** | API и DTO сгенерированы из OpenAPI спецификации |
| 4 | **Swagger UI** | Интерактивная документация API доступна по умолчанию |
| 5 | **Унифицированная обработка ошибок** | Ошибки Binance и приложения объединены под `BaseApplicationException` с глобальным `@RestControllerAdvice` |
| 6 | **Настроенные таймауты** | `connect-timeout: PT3S` и `read-timeout: PT5S` — предотвращают бесконечное ожидание ответа от Binance |
| 7 | **Настроенные Checkstyle и Archunit правила** | В эпоху разработки с ИИ крайне важно следить за качеством кода, поэтому в проекте настроены правила Checkstyle и Archunit |
