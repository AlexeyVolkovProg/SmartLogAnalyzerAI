package com.assistant.smartloganalyzerai.service;

import com.assistant.smartloganalyzerai.dto.api.LogEntryRequestDto;
import com.assistant.smartloganalyzerai.dto.api.LogSearchRequestDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class LogVectorService {
    private final VectorStore vectorStore;

    public LogVectorService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public int store(List<LogEntryRequestDto> logs) {
        List<Document> documents = logs.stream()
                .map(this::toDocument)
                .toList();

        vectorStore.add(documents);
        log.info("Stored {} log entries in vector store", documents.size());
        return documents.size();
    }

    public List<Document> search(LogSearchRequestDTO request) {
        String query = request.getQuery();
        int topK = request.getTopK() != null ? request.getTopK() : 10;

        StringBuilder filter = new StringBuilder();

        if (request.getServiceName() != null && !request.getServiceName().isBlank()) {
            filter.append(String.format("service_name == '%s'", request.getServiceName()));
        }

//        if (request.getFrom() != null) {
//            if (!filter.isEmpty()) filter.append(" && ");
//            filter.append(String.format("timestamp_epoch >= %d", request.getFrom().getEpochSecond()));
//        }
//
//        if (request.getTo() != null) {
//            if (!filter.isEmpty()) filter.append(" && ");
//            filter.append(String.format("timestamp_epoch <= %d", request.getTo().getEpochSecond()));
//        }

        SearchRequest.Builder builder = SearchRequest.builder()
                .query(query)
                .topK(topK)
                .similarityThreshold(0.3);

        if (!filter.isEmpty()) {
            builder.filterExpression(filter.toString());
        }

        List<Document> results = vectorStore.similaritySearch(builder.build());
        log.info("Search for '{}' returned {} results", query, results.size());
        return results;
    }

    public List<Document> search(String query, String serviceName, Instant from, Instant to, int topK) {
        StringBuilder filter = new StringBuilder();

        if (serviceName != null && !serviceName.isBlank()) {
            filter.append(String.format("service_name == '%s'", serviceName));
        }

        if (from != null) {
            if (!filter.isEmpty()) filter.append(" && ");
            filter.append(String.format("timestamp_epoch >= %d", from.toEpochMilli()));
        }

        if (to != null) {
            if (!filter.isEmpty()) filter.append(" && ");
            filter.append(String.format("timestamp_epoch <= %d", to.toEpochMilli()));
        }

        SearchRequest.Builder builder = SearchRequest.builder()
                .query(query)
                .topK(topK)
                .similarityThreshold(0.3);

        if (!filter.isEmpty()) {
            builder.filterExpression(filter.toString());
        }

        List<Document> results = vectorStore.similaritySearch(builder.build());
        log.info("Search for '{}' returned {} results", query, results.size());
        return results;
    }


    public List<Document> search(String query, String serviceName) {
        return search(query, serviceName, null, null, 10);
    }

    public List<Document> search(String query) {
        return search(query, null, null, null, 10);
    }

    private Document toDocument(LogEntryRequestDto dto) {
        Map<String, Object> metadata = Map.of(
                "service_name", dto.getServiceName(),
                "log_level", dto.getLogLevel(),
                "timestamp_iso", dto.getTimestamp().toString(),
                "timestamp_epoch", dto.getTimestamp().getEpochSecond()
        );

        String content = String.format("[%s] [%s] %s",
                dto.getLogLevel(),
                dto.getServiceName(),
                dto.getMessage());

        return Document.builder()
                .text(content)
                .metadata(metadata)
                .build();
    }
}
