package com.assistant.smartloganalyzerai.service;

import com.assistant.smartloganalyzerai.advisor.EnrichSemanticQueryAdvisor;
import com.assistant.smartloganalyzerai.dto.api.IncidentAnalysisRequestDTO;
import com.assistant.smartloganalyzerai.dto.api.IncidentAnalysisResponseDTO;
import com.assistant.smartloganalyzerai.dto.api.LogSearchRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class IncidentAnalysisService {
    
    private static final String ANALYSIS_PROMPT = """
            Analyze the following incident based on the provided logs.
            
                    Service: {service_name}
                    Service description: {service_description}
                    Incident: {incident_description}
                    Mode: {analysis_mode}
            
                    Provide your analysis as a JSON object with this exact structure, no markdown:
                    {{
                      "root_cause": "concise root cause statement",
                      "detailed_explanation": "detailed technical explanation of what happened",
                      "evidence": ["log evidence 1", "log evidence 2"],
                      "recommended_actions": ["action 1", "action 2"]
                    }}
            """;

    private final ChatClient chatClient;
    private final LogVectorService logVectorService;

    @SuppressWarnings("unchecked")
    public IncidentAnalysisResponseDTO analyze(IncidentAnalysisRequestDTO request) {
        long startTime = System.currentTimeMillis();
        String analysisId = UUID.randomUUID().toString();

        // Подготавливаем фильтры для гибридного поиска
        LogSearchRequestDTO searchFilters = new LogSearchRequestDTO();
        searchFilters.setServiceName(request.getServiceName());
        searchFilters.setTopK(request.getTopK());
        if (request.getTimeRange() != null) {
            searchFilters.setFrom(request.getTimeRange().getFrom());
            searchFilters.setTo(request.getTimeRange().getTo());
        }

        // Контекст, который advisor получит через request.context()
        Map<String, Object> advisorContext = new HashMap<>();
        advisorContext.put("service_name", request.getServiceName());
        advisorContext.put("service_description", request.getServiceDescription());
        advisorContext.put("incident_description", request.getIncidentDescription());
        advisorContext.put("analysis_mode", request.getMode().name());
        advisorContext.put("search_filters", searchFilters);

        // Создаём advisor
        EnrichSemanticQueryAdvisor enrichSemanticQueryAdvisor = new EnrichSemanticQueryAdvisor(chatClient, logVectorService);

        // Вызываем LLM с advisor'ом
        IncidentAnalysisResponseDTO.LlmResponseDTO llmResponse = chatClient.prompt()
                .user(u -> u.text(ANALYSIS_PROMPT)
                        .param("service_name", Objects.toString(request.getServiceName(), "unknown"))
                        .param("service_description", Objects.toString(request.getServiceDescription(), "not provided"))
                        .param("incident_description", Objects.toString(request.getIncidentDescription(), "not provided"))
                        .param("analysis_mode", request.getMode().name()))
                .advisors(enrichSemanticQueryAdvisor,
                        SimpleLoggerAdvisor.builder().order(1).build())
                .advisors(a -> a.params(advisorContext))
                .call()
                .entity(IncidentAnalysisResponseDTO.LlmResponseDTO.class);

        // Извлекаем результаты поиска из advisor context
        List<Document> foundDocs = (List<Document>) advisorContext
                .getOrDefault("log_search_documents", List.of());
        List<String> searchKeywords = (List<String>) advisorContext
                .getOrDefault("log_search_queries", List.of());

        // Формируем related logs для ответа
        List<IncidentAnalysisResponseDTO.RelatedLogDTO> relatedLogs = foundDocs.stream()
                .map(doc -> IncidentAnalysisResponseDTO.RelatedLogDTO.builder()
                        .id(doc.getId())
                        .serviceName((String) doc.getMetadata().get("service_name"))
                        .logLevel((String) doc.getMetadata().get("log_level"))
                        .message(doc.getText())
                        .similarityScore(doc.getScore() != null ? doc.getScore() : 0.0)
                        .build())
                .toList();

        long tookMs = System.currentTimeMillis() - startTime;
        log.info("Incident analysis {} completed in {}ms, found {} related logs",
                analysisId, tookMs, relatedLogs.size());

        return IncidentAnalysisResponseDTO.builder()
                .analysisId(analysisId)
                .tookMs(tookMs)
                .searchKeywords(searchKeywords)
                .llmResponse(llmResponse)
                .relatedLogs(relatedLogs)
                .build();
    }
}
