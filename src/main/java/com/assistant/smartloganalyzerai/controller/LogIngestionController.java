package com.assistant.smartloganalyzerai.controller;

import com.assistant.smartloganalyzerai.dto.LogEntryRequestDto;
import com.assistant.smartloganalyzerai.service.LogIngestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
public class LogIngestionController {

    private final LogIngestionService logIngestionService;

    /**
     * Пакетная загрузка логов с Fluent Bit
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> ingestBatch(@RequestBody List<LogEntryRequestDto> logs) {
        try{
            log.info("Received batch of {} log entries", logs.size());

            int saved = logIngestionService.ingest(logs);

            return ResponseEntity.ok(Map.of(
                    "status", "ok",
                    "ingested", saved
            ));
        }catch (Exception e){
            log.info("Error in batch of {} log entries, message {}", logs.size(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of());
        }
    }

    /**
     * Одиночная загрузка логов с Fluent Bit
     */
    @PostMapping("/single")
    public ResponseEntity<Map<String, Object>> ingestSingle(@RequestBody LogEntryRequestDto logEntry) {
        log.debug("Received single log entry: {}", logEntry);

        int saved = logIngestionService.ingest(List.of(logEntry));

        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "ingested", saved
        ));
    }
}
