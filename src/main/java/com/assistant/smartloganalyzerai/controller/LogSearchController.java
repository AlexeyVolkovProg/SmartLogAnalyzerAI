package com.assistant.smartloganalyzerai.controller;

import com.assistant.smartloganalyzerai.dto.LogSearchRequestDTO;
import com.assistant.smartloganalyzerai.service.LogVectorService;
import org.springframework.ai.document.Document;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/logs")
public class LogSearchController {
    private final LogVectorService logVectorService;

    public LogSearchController(LogVectorService logVectorService) {
        this.logVectorService = logVectorService;
    }

    /**
     * Для тестов поиска по логам и замеров по времени
     */
    @PostMapping("/search")
    public ResponseEntity<List<Map<String, Object>>> search(@RequestBody LogSearchRequestDTO request) {
        List<Document> results = logVectorService.search(request.getQuery(), request.getServiceName());

        List<Map<String, Object>> response = results.stream()
                .map(doc -> Map.of(
                        "id", doc.getId(),
                        "content", doc.getText(),
                        "metadata", doc.getMetadata(),
                        "score", doc.getScore() != null ? doc.getScore() : 0.0
                ))
                .toList();

        return ResponseEntity.ok(response);
    }
}
