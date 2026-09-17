package com.kurniadhi.mcp.basic.api;

import com.kurniadhi.mcp.basic.api.dto.DocumentRequest;
import com.kurniadhi.mcp.basic.api.dto.DocumentResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping("/documents")
    public ResponseEntity<DocumentResponse> create(@RequestBody DocumentRequest request) {
        DocumentResponse created = documentService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/documents/{key}")
    public DocumentResponse get(@PathVariable String key) {
        return documentService.find(key)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found: " + key));
    }

    @GetMapping("/keys")
    public List<String> keys() {
        return documentService.keys();
    }
}