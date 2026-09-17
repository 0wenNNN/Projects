# basic — Project Overview

Spring Boot 4.1.0 web service with SQLite vector search via sqlite-vec.

## Database

| Property | Value |
|----------|-------|
| Engine | SQLite 3 (WAL mode) |
| File | `${user.home}/search_engine.db` |
| On this machine | `/Users/wcw-e/search_engine.db` |

### Connection

- URL: `jdbc:sqlite:${user.home}/search_engine.db?journal_mode=WAL`
- Pool: HikariCP (max 10, min 2)
- Extension: sqlite-vec (FLOAT32, 384-dim, cosine distance)
- Init: `load_extension(path, 'sqlite3_vector_init')` + `vector_init('documents', 'embed', 'type=FLOAT32,dimension=384,distance=cosine')`

## Schema

### `documents`

| Column | Type | Constraints |
|--------|------|-------------|
| id | INTEGER | PRIMARY KEY AUTOINCREMENT |
| key | TEXT | NOT NULL, UNIQUE |
| value | TEXT | NOT NULL |
| embed | BLOB | NOT NULL — FLOAT32[384] |

Created by `schema.sql` on every startup (`spring.sql.init.mode: always`).

## API

REST controllers in `com.kurniadhi.mcp.basic.api`.

| Method | Path | Action |
|--------|------|--------|
| POST | `/api/documents` | Insert document (JSON body: `key`, `value`). `201 Created`; `409 Conflict` if key exists. |
| GET | `/api/documents/{key}` | Read document by key. `200 OK` or `404 Not Found`. |
| GET | `/api/keys` | List all keys. |

## Source Files

| File | Purpose |
|------|---------|
| `BasicApplication.java` | `@SpringBootApplication`, package-private main |
| `DataSourceConfig.java` | Extracts sqlite-vec native lib, wires HikariCP with `enable_load_extension=true` |
| `api/DocumentController.java` | `@RestController`, 3 REST endpoints |
| `api/DocumentService.java` | `@Service`, CRUD via `JdbcTemplate` |
| `api/dto/DocumentRequest.java` | Request body record (`key`, `value`) |
| `api/dto/DocumentResponse.java` | Response record (`key`, `value`) |
| `BasicApplicationTests.java` | `@SpringBootTest` context-load smoke test |
| `DocumentControllerTests.java` | MockMvc endpoint tests (create/read/list, 404, 409) |

## Config

`src/main/resources/application.yaml` — single source of truth.
