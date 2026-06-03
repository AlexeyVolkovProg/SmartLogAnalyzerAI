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

                    IMPORTANT: You MUST respond entirely in Russian language. All field values must be in Russian.

                    Provide your analysis as a JSON object with this exact structure, no markdown:
                    {{
                      "root_cause": "краткое описание первопричины",
                      "detailed_explanation": "подробное техническое объяснение произошедшего",
                      "evidence": ["доказательство из логов 1", "доказательство из логов 2"],
                      "recommended_actions": ["действие 1", "действие 2"]
                    }}
            """;

    private final ChatClient chatClient;
    private final LogVectorService logVectorService;

    @SuppressWarnings("unchecked")
    public IncidentAnalysisResponseDTO analyze(IncidentAnalysisRequestDTO request) {
        long startTime = System.currentTimeMillis();
        String analysisId = UUID.randomUUID().toString();

        LogSearchRequestDTO searchFilters = new LogSearchRequestDTO();
        searchFilters.setServiceName(request.getServiceName());
        searchFilters.setTopK(request.getTopK());
        if (request.getTimeRange() != null) {
            searchFilters.setFrom(request.getTimeRange().getFrom());
            searchFilters.setTo(request.getTimeRange().getTo());
        }

        Map<String, Object> advisorContext = new HashMap<>();
        advisorContext.put("service_name", request.getServiceName());
        advisorContext.put("service_description", request.getServiceDescription());
        advisorContext.put("incident_description", request.getIncidentDescription());
        advisorContext.put("analysis_mode", request.getMode().name());
        advisorContext.put("search_filters", searchFilters);

        EnrichSemanticQueryAdvisor enrichSemanticQueryAdvisor = new EnrichSemanticQueryAdvisor(chatClient, logVectorService);

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

        List<Document> foundDocs = (List<Document>) advisorContext
                .getOrDefault("log_search_documents", List.of());
        List<String> searchKeywords = (List<String>) advisorContext
                .getOrDefault("log_search_queries", List.of());

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
