# ₿ CoinPulse

Сервис для отслеживания и анализа исторических цен криптовалют с Binance

Сервис автоматически собирает средние цены торговых пар с биржи Binance, сохраняет их в PostgreSQL и предоставляет доступ через REST API. 

Поддерживает расчёт средней цены за период, пагинированный просмотр записей и определение тренда.

---

## 🚀 Быстрый старт

### Запуск одной командой

```bash
docker compose up --build
```

Это поднимет всё необходимое: PostgreSQL, применит миграции БД и запустит приложение.

Приложение будет доступно на `http://localhost:8080`

Документация Swagger UI: `http://localhost:8080/swagger-ui.html`

### Пошаговый запуск

```bash
# 1. Собрать образ и поднять все сервисы
docker compose up --build

# 2. Остановить всё
docker compose down

# 3. Остановить + удалить volumes (сброс БД)
docker compose down -v
```

### Локальная разработка (без Docker)

```bash
# 1. Запустить PostgreSQL и програть Liquibase миграции
docker compose up -d postgres liquibase

> Дождись, пока Liquibase завершит миграции. PostgreSQL продолжит работать.

# 2. Установить переменные окружения в терминале, где будет запущено приложение (для примера PowerShell):
\```powershell
$env:SPRING_DATASOURCE_URL = "jdbc:postgresql://localhost:5432/coin_pulse"
$env:SPRING_DATASOURCE_USERNAME = "postgres"
$env:SPRING_DATASOURCE_PASSWORD = "postgres"

$env:BINANCE_BASE_URL = "https://api.binance.com"
$env:BINANCE_SYMBOLS = "BNBUSDT,BTCUSDT,ETHUSDT"
$env:BINANCE_POLL_INTERVAL = "PT30S"
\```

# 3. Запустить приложение
./gradlew bootRun

# 4. Остановить приложение
В терминале, где запущен bootRun, нажать Ctrl + C.

# 5. Остановить PostgreSQL
\```powershell
docker compose down
\```
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
