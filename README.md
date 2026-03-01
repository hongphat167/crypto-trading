# Crypto Trading Platform

Crypto trading system built with Spring Boot and H2 in-memory database. Aggregates real-time prices from Binance and
Huobi, supports BUY/SELL for ETHUSDT and BTCUSDT pairs.

## Tech Stack

- Java 21, Spring Boot 4.0.3, Spring Data JPA, Spring WebFlux
- H2 Database (in-memory), Lombok, SpringDoc OpenAPI 3.0.1
- Gradle, JUnit 5 + Mockito

## Architecture

CQRS (Command Query Responsibility Segregation) pattern.

## Getting Started

### Prerequisites

- Java 21+

### Run

```bash
./gradlew bootRun
```

Windows:

```powershell
.\gradlew.bat bootRun
```

Application runs at: http://localhost:8080

### Access

| Resource   | URL                                   |
|------------|---------------------------------------|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| H2 Console | http://localhost:8080/h2-console      |

**H2 Credentials:** JDBC URL `jdbc:h2:mem:cryptodb` / Username `phat` / Password `phat`

## API Endpoints

| Method | Endpoint                       | Description                       |
|--------|--------------------------------|-----------------------------------|
| `GET`  | `/api/prices`                  | Get latest best aggregated prices |
| `POST` | `/api/trades`                  | Execute a trade (BUY/SELL)        |
| `GET`  | `/api/trades/history/{userId}` | Get trade history                 |
| `GET`  | `/api/wallets/{userId}`        | Get wallet balances               |

### Trade Request Example

```json
{
  "userId": 1,
  "tradingPair": "ETHUSDT",
  "tradeType": "BUY",
  "quantity": 0.5,
  "username": "hong_phat"
}
```

## Default Data

On startup, a default user `hong_phat` is created with:

- **USDT**: 50,000
- **ETH**: 0
- **BTC**: 0

## Run Tests

```bash
./gradlew test
```

## Author

**Hong Phat** - hongphat167@gmail.com
