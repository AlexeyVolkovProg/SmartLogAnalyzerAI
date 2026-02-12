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

    @Transactional
    public int ingest(List<LogEntryRequestDto> dto) {
        List<LogEntry> entities = dto.stream()
                .map(this::toEntity)
                .toList();

        repository.saveAll(entities);
        log.info("Ingested {} log entries", entities.size());
        return entities.size();
    }

    private LogEntry toEntity(LogEntryRequestDto dto) {
        return LogEntry.builder().serviceName(dto.getServiceName())
                .timestamp(dto.getTimestamp())
                .logLevel(dto.getLogLevel())
                .message(dto.getMessage())
                .build();
    }
}
