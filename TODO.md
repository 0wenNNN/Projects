# TODO — REST API

Create REST API in `com.kurniadhi.mcp.basic.api` package.

Use `JdbcTemplate` (Spring Data JDBC repos excluded — SQLite has no dialect). Don't add `@DataJdbcTest` or repository interfaces.

## Endpoints — DONE

| Method | Path | Action |
|--------|------|--------|
| POST | `/api/documents` | Insert document (key, value). Ignore embed. |
| GET | `/api/documents/{key}` | Read document by key. |
| GET | `/api/keys` | List all keys. |

## Implementation

1. `DocumentController` — `@RestController` with 3 endpoints
2. `DocumentService` — `@Service`, injects `JdbcTemplate`, SQL queries
3. Request/response DTOs in `com.kurniadhi.mcp.basic.api.dto`

## Schema reminder

`documents` table — `id` (auto), `key` (unique), `value`, `embed` (BLOB). Insert w/o embed: use empty byte array or `NULL`.

## Files to create

```
src/main/java/com/kurniadhi/mcp/basic/api/
├── DocumentController.java
├── DocumentService.java
└── dto/
    └── DocumentRequest.java
```
