package com.kurniadhi.mcp.basic.api;

import com.kurniadhi.mcp.basic.api.dto.DocumentRequest;
import com.kurniadhi.mcp.basic.api.dto.DocumentResponse;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class DocumentService {

    private static final byte[] EMPTY_EMBED = new byte[0];

    private final JdbcTemplate jdbcTemplate;

    public DocumentService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public DocumentResponse create(DocumentRequest request) {
        validate(request);
        int inserted = jdbcTemplate.update(
                "INSERT OR IGNORE INTO documents (key, value, embed) VALUES (?, ?, ?)",
                request.key(), request.value(), EMPTY_EMBED);
        if (inserted == 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Key already exists: " + request.key());
        }
        return new DocumentResponse(request.key(), request.value());
    }

    public Optional<DocumentResponse> find(String key) {
        if (key == null || key.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Key must not be blank");
        }
        return jdbcTemplate.query(
                "SELECT key, value FROM documents WHERE key = ?",
                (rs, rowNum) -> new DocumentResponse(rs.getString("key"), rs.getString("value")),
                key).stream().findFirst();
    }

    public List<String> keys() {
        return jdbcTemplate.queryForList("SELECT key FROM documents ORDER BY key", String.class);
    }

    private void validate(DocumentRequest request) {
        if (request == null || request.key() == null || request.key().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "key must not be blank");
        }
        if (request.value() == null || request.value().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "value must not be blank");
        }
    }
}