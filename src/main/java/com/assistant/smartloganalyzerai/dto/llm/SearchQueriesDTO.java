package com.assistant.smartloganalyzerai.dto.llm;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

/**
 * Структурированный ответ от LLM — оптимальные поисковые запросы для vector search.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchQueriesDTO {

    /**
     * Список поисковых запросов, сгенерированных LLM.
     * Каждый запрос оптимизирован для семантического поиска в логах.
     */
    @JsonProperty("search_queries")
    private List<String> searchQueries;

    /**
     * Краткое обоснование, почему выбраны именно эти запросы.
     */
    private String reasoning;
}
