package com.assistant.smartloganalyzerai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LogSearchRequestDTO {
    private String query;

    @JsonProperty("service_name")
    private String serviceName;

    @JsonProperty("log_level")
    private String logLevel;

    private Instant from;

    private Instant to;

    @JsonProperty("top_k")
    private Integer topK = 10;
}
