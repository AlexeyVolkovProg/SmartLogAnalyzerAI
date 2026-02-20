package com.assistant.smartloganalyzerai.dto.api;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.*;

import java.time.Instant;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LogEntryRequestDto {
    @JsonAlias({"date", "timestamp"})
    private Instant timestamp;

    @JsonAlias("service_name")
    private String serviceName;

    @JsonAlias("log_level")
    private String logLevel;

    private String message;
}
