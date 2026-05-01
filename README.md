# Banco Digital - Reporting Service

The Reporting Service provides analytics, reporting, and materialized views for the Banco Digital platform. It consumes domain events from Kafka to build reporting views and serves them via REST API.

## Architecture

- **Hexagonal/Clean Architecture** - Domain-driven design with ports and adapters
- **Event-Driven Materialization** - Kafka consumers build reporting views
- **Resilience4j** - Circuit breaker for database failures
- **JPA/PostgreSQL** - Persistent reporting data storage
- **JWT Authentication** - Validates tokens for query endpoints
- **Flyway** - Database migrations

## Running Locally

### Prerequisites

- Java 17+
- Maven 3.9+
- PostgreSQL 16+
- Kafka (for event consumption)
- Docker (optional)

### Build

```bash
mvn clean package -DskipTests
```

### Run

```bash
# Set environment variables
export APP_PROFILE=local
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=banco_digital_reporting
export DB_USERNAME=report_user
export DB_PASSWORD=your_password
export KAFKA_BOOTSTRAP_SERVERS=localhost:9092
export JWT_SECRET=your_base64_encoded_256bit_secret_here

mvn spring-boot:run
```

### Docker

```bash
docker build -t banco-digital-reporting .
docker run -p 8083:8083 \
  -e APP_PROFILE=local \
  -e DB_HOST=localhost \
  -e DB_NAME=banco_digital_reporting \
  -e DB_USERNAME=report_user \
  -e DB_PASSWORD=your_password \
  -e KAFKA_BOOTSTRAP_SERVERS=localhost:9092 \
  -e JWT_SECRET=your_secret \
  banco-digital-reporting
```

## API Endpoints

| Method | Path | Description | Auth |
|--------|------|-------------|------|
| GET | `/api/v1/reportes/saldo-total/{cedula}` | Get total balance for client | Yes |
| GET | `/api/v1/reportes/resumen/{cedula}` | Get client financial summary | Yes |
| GET | `/api/v1/reportes/transacciones/{cedula}` | Get client transactions | Yes |
| GET | `/api/v1/reportes/estado-cuenta/{cedula}` | Get account statement | Yes |

## Kafka Topics

| Topic | Purpose |
|-------|---------|
| `banco-digital-events` | Main event bus (consumed) |
| `banco-digital-events-dlq` | Dead letter queue |
| `reporting-events-pending` | Fallback queue when DB circuit breaker is open |

## Swagger UI

Available at: `http://localhost:8083/swagger-ui.html`

## Testing

```bash
mvn test
```
