package com.assistant.smartloganalyzerai.dto.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidentAnalysisResponseDTO {

    @JsonProperty("analysis_id")
    private String analysisId;

    @JsonProperty("took_ms")
    private Long tookMs;

    @JsonProperty("search_keywords")
    private List<String> searchKeywords;

    @JsonProperty("llm_response")
    private LlmResponseDTO llmResponse;

    @JsonProperty("related_logs")
    private List<RelatedLogDTO> relatedLogs;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LlmResponseDTO {

        @JsonProperty("root_cause")
        private String rootCause;

        @JsonProperty("detailed_explanation")
        private String detailedExplanation;

        private List<String> evidence;

        @JsonProperty("recommended_actions")
        private List<String> recommendedActions;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RelatedLogDTO {

        private String id;

        private Instant timestamp;

        @JsonProperty("service_name")
        private String serviceName;

        @JsonProperty("log_level")
        private String logLevel;

        private String message;

        @JsonProperty("similarity_score")
        private Double similarityScore;
    }
}