package com.assistant.smartloganalyzerai.service;

import com.assistant.smartloganalyzerai.dto.LogEntryRequestDto;
import com.assistant.smartloganalyzerai.entity.LogEntry;
import com.assistant.smartloganalyzerai.repository.LogEntryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogIngestionService {

    private final LogEntryRepository repository;
    private final LogVectorService logVectorService;

    @Transactional
    public int ingest(List<LogEntryRequestDto> logs) {
        List<LogEntry> entities = logs.stream()
                .map(this::toEntity)
                .toList();

        repository.saveAll(entities);

        long start = System.currentTimeMillis();
        logVectorService.store(logs);
        long end = System.currentTimeMillis();

        log.info("Ingest embedding batch store time: {} ms, batch size: {}",
                end - start, logs.size());

        log.info("Ingested {} log entries", entities.size());
        return entities.size();
    }

    private LogEntry toEntity(LogEntryRequestDto log) {
        return LogEntry.builder().serviceName(log.getServiceName())
                .timestamp(log.getTimestamp())
                .logLevel(log.getLogLevel())
                .message(log.getMessage())
                .build();
    }
}
