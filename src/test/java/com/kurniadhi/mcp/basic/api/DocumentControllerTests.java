package com.kurniadhi.mcp.basic.api;

import com.kurniadhi.mcp.basic.api.dto.DocumentRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DocumentControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createReadAndListKeys() throws Exception {
        String key = "test-" + System.nanoTime();
        String body = objectMapper.writeValueAsString(new DocumentRequest(key, "hello"));

        mockMvc.perform(post("/api/documents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.key").value(key))
                .andExpect(jsonPath("$.value").value("hello"));

        mockMvc.perform(get("/api/documents/{key}", key))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.key").value(key))
                .andExpect(jsonPath("$.value").value("hello"));

        mockMvc.perform(get("/api/keys"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void missingDocumentReturns404() throws Exception {
        mockMvc.perform(get("/api/documents/{key}", "does-not-exist-" + System.nanoTime()))
                .andExpect(status().isNotFound());
    }

    @Test
    void duplicateKeyReturnsConflict() throws Exception {
        String key = "dup-" + System.nanoTime();
        String body = objectMapper.writeValueAsString(new DocumentRequest(key, "first"));

        mockMvc.perform(post("/api/documents").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/documents").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
    }
}