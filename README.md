# Coupon Redemption Service

REST service for managing discount coupons. Supports coupon creation and usage registration with country-based restrictions via IP geolocation.

## Running

Requirements: Java 21, Maven, Docker

```bash
docker compose up -d
./mvnw clean install
./mvnw spring-boot:run
```

Tests:

```bash
./mvnw test
```

Integration tests use Testcontainers — Docker must be available, no local database needed.

## API

### Create coupon

```
POST /api/coupons
```

```json
{
  "code": "SUMMER25",
  "maxUsages": 100,
  "countryCode": "PL"
}
```

### Redeem coupon

```
POST /api/coupons/{code}/redemption
```

```json
{
  "userId": "user-123"
}
```

The user's country is resolved automatically from the request IP. Behind a load balancer, the service reads `X-Forwarded-For` (`server.forward-headers-strategy: framework`). For local testing, the header can be set manually:

```bash
curl -X POST http://localhost:8080/api/coupons/SUMMER25/redemption \
  -H "Content-Type: application/json" \
  -H "X-Forwarded-For: 185.107.56.1" \
  -d '{"userId": "user-123"}'
```

### Response codes

| Case | Status |
|---|---|
| Success | 200 |
| Coupon not found | 404 |
| Coupon reserved for a different country | 403 |
| Usage limit reached | 410 |
| User already redeemed this coupon | 409 |
| Country could not be resolved (GeoIP unavailable) | 503 |

## Design decisions

### Architecture

A layered architecture (controller → service → repository) was chosen over hexagonal. The domain is small and stable — ports and adapters would add indirection without a clear benefit here.

### Concurrency

The core challenge is multiple users attempting to redeem the same coupon simultaneously. This is handled with a conditional atomic UPDATE:

```sql
UPDATE coupons
SET current_usages = current_usages + 1
WHERE code = :code AND current_usages < max_usages
```

If the UPDATE affects 0 rows, the coupon is exhausted. This avoids pessimistic locking — the database enforces "first come first served" with minimal lock hold time.

The optional one-user-one-use rule is enforced via a `UNIQUE (user_id, coupon_id)` constraint on `coupons_usage`. The usage record INSERT happens before the counter UPDATE — a uniqueness violation rolls back the transaction before the counter is touched.

### Country detection

[ip-api.com](http://ip-api.com) is used for geolocation — free, no registration required. The alternative would be MaxMind GeoLite2 (offline `.mmdb` file, no network dependency), but it requires account registration and bundling the database file.

The HTTP client has a 2s connect / 3s read timeout configured. If GeoIP is unavailable, the service returns 503 — a deliberate fail-closed decision to avoid skipping country validation silently.

`IPApiGeolocationParser` also implements in-memory rate limiting based on `X-Rl` and `X-Ttl` response headers from ip-api, to avoid sending requests when the API is blocking them.

### Next steps

- Circuit breaker (e.g. Resilience4j) on the GeoIP client with a fallback strategy
- Retry with exponential backoff for transient network errors
- Endpoint for checking coupon status
- Dockerize the application
- Redis cache for exhausted coupons with 24h TTL — under high traffic (e.g. Black Friday) an exhausted coupon would skip the database entirely and return 410 straight from cache
- Swagger / OpenAPI documentation
- Extract `userId` from the authentication token instead of accepting it as a request parameter, if the endpoint is meant to be called directly by end users
