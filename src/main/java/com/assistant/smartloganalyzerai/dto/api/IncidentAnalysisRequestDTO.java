package com.assistant.smartloganalyzerai.dto.api;

import com.assistant.smartloganalyzerai.dto.enums.AnalysisMode;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.validation.constraints.NotNull;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidentAnalysisRequestDTO {

    @JsonProperty("service_name")
    private String serviceName;

    @NotNull
    private AnalysisMode mode;

    @JsonProperty("service_description")
    private String serviceDescription;

    @JsonProperty("incident_description")
    private String incidentDescription;

    @JsonProperty("time_range")
    private TimeRangeDTO timeRange;

    @JsonProperty("top_k")
    @Builder.Default
    private Integer topK = 10;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeRangeDTO{
        private Instant from;
        private Instant to;
    }
}