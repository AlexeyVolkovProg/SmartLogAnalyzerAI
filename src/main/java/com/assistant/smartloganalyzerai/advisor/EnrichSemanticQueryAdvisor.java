package com.assistant.smartloganalyzerai.advisor;

import com.assistant.smartloganalyzerai.dto.api.LogSearchRequestDTO;
import com.assistant.smartloganalyzerai.dto.llm.SearchQueriesDTO;
import com.assistant.smartloganalyzerai.service.LogVectorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Advisor, который перехватывает запрос пользователя об инциденте,
 * генерирует оптимальные поисковые запросы через LLM,
 * находит релевантные логи и добавляет их в контекст промпта исходного промпта анализа инцидента
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EnrichSemanticQueryAdvisor  implements BaseAdvisor {

    private static final String QUERY_GENERATION_PROMPT = """
             You are a log search query optimizer. Given an incident description and service context,
                    generate 3-5 short, precise search queries optimized for semantic similarity search
                    in a log database.
            
                    Rules:
                    - Each query should target a different aspect of the potential root cause
                    - Use technical terms that would appear in actual log messages
                    - Keep queries short (3-8 words each) for best embedding match
                    - Include error patterns, component names, and failure modes
                    - Do NOT use boolean operators or special syntax
            
                    Service: {service_name}
                    Service description: {service_description}
                    Incident description: {incident_description}
                    Analysis mode: {analysis_mode}
            
                    Respond ONLY with a valid JSON object in this exact format, no markdown:
                    {{"search_queries": ["query1", "query2", "query3"], "reasoning": "brief explanation"}}
            """;

    private final ChatClient chatClient;
    private final LogVectorService logVectorService;

    @Override
    public int getOrder() {
        return 0; // Выполняется первым
    }

    /**
     * Генерируем поисковые запросы, ищем логи, обогащаем prompt, чтобы прокинуть его дальше по цепи Advisors
     */
    @Override
    public ChatClientRequest before(ChatClientRequest request, AdvisorChain advisorChain) {
        Map<String, Object> context = request.context();

        String serviceName = (String) context.getOrDefault("service_name", "");
        String serviceDescription = (String) context.getOrDefault("service_description", "");
        String incidentDescription = (String) context.getOrDefault("incident_description", "");
        String analysisMode = (String) context.getOrDefault("analysis_mode", "INCIDENT_DESCRIPTION");

        LogSearchRequestDTO searchFilters = (LogSearchRequestDTO) context.get("search_filters");

        //Генерируем оптимальные поисковые запросы через LLM
        SearchQueriesDTO searchQueries = generateSearchQueriesWithLLM(
                serviceName, serviceDescription, incidentDescription, analysisMode);

        log.info("Generated {} search queries: {}",
                searchQueries.getSearchQueries().size(),
                searchQueries.getSearchQueries());

        //Выполняем гибридный поиск по каждому запросу, убирая дубликаты
        List<Document> allResults = new ArrayList<>();
        Set<String> seenIds = new HashSet<>();

        for (String query : searchQueries.getSearchQueries()) {
            LogSearchRequestDTO searchRequest = getLogSearchRequestDTO(query, searchFilters);

            List<Document> results = logVectorService.search(searchRequest);
            for (Document doc : results) {
                if (seenIds.add(doc.getId())) {
                    allResults.add(doc);
                }
            }
        }

        log.info("Found {} unique relevant logs across all queries", allResults.size());

        //Сохраняем результаты в контекст (для использования в сервисе после ответа)
        context.put("log_search_documents", allResults);
        context.put("log_search_queries", searchQueries.getSearchQueries());

        //Формируем контекст с найденными логами
        String logContext = allResults.stream()
                .map(doc -> String.format("[score=%.3f] %s",
                        doc.getScore() != null ? doc.getScore() : 0.0,
                        doc.getText()))
                .collect(Collectors.joining("\n"));

        //Обогащаем user message контекстом, состоящем из найденных нами логов
        String enrichedUserText = getString(request, allResults, logContext);

        return request.mutate()
                .prompt(request.prompt().augmentUserMessage(enrichedUserText))
                .build();
    }

    private static String getString(ChatClientRequest request, List<Document> allResults, String logContext) {
        String originalUserText = request.prompt().getContents();
        return String.format("""
                %s
                
                RELEVANT LOGS FOUND (%d entries)
                %s
                END OF LOGS
                
                Based on these logs, provide your analysis.
                """,
                originalUserText,
                allResults.size(),
                logContext.isEmpty() ? "No relevant logs found." : logContext);
    }

    private static LogSearchRequestDTO getLogSearchRequestDTO(String query, LogSearchRequestDTO searchFilters) {
        LogSearchRequestDTO searchRequest = new LogSearchRequestDTO();
        searchRequest.setQuery(query);
        searchRequest.setTopK(searchFilters != null ? searchFilters.getTopK() : 10);

        if (searchFilters != null) {
            searchRequest.setServiceName(searchFilters.getServiceName());
            searchRequest.setLogLevel(searchFilters.getLogLevel());
            searchRequest.setFrom(searchFilters.getFrom());
            searchRequest.setTo(searchFilters.getTo());
        }
        return searchRequest;
    }

    /**
     * Пропускаем ответ без изменений.
     */
    @Override
    public ChatClientResponse after(ChatClientResponse response, AdvisorChain advisorChain) {
        return response;
    }

    private SearchQueriesDTO generateSearchQueriesWithLLM(
            String serviceName,
            String serviceDescription,
            String incidentDescription,
            String analysisMode) {
        try {
            return chatClient.prompt()
                    .user(u -> u.text(QUERY_GENERATION_PROMPT)
                            .param("service_name", serviceName != null ? serviceName : "unknown")
                            .param("service_description", serviceDescription != null ? serviceDescription : "not provided")
                            .param("incident_description", incidentDescription != null ? incidentDescription : "not provided")
                            .param("analysis_mode", analysisMode))
                    .call()
                    .entity(SearchQueriesDTO.class);
        } catch (Exception e) {
            log.error("Failed to generate search queries via LLM, using incident description as fallback", e);
            return SearchQueriesDTO.builder()
                    .searchQueries(List.of(incidentDescription != null ? incidentDescription : "error"))
                    .reasoning("Fallback: using raw incident description")
                    .build();
        }
    }
}
