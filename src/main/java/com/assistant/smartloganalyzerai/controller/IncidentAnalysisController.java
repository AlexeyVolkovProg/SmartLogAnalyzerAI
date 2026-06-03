package com.assistant.smartloganalyzerai.controller;

import com.assistant.smartloganalyzerai.dto.api.IncidentAnalysisRequestDTO;
import com.assistant.smartloganalyzerai.dto.api.IncidentAnalysisResponseDTO;
import com.assistant.smartloganalyzerai.service.IncidentAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/incidents")
public class IncidentAnalysisController {
    private final IncidentAnalysisService service;

    @PostMapping("/analyze")
    public ResponseEntity<IncidentAnalysisResponseDTO> analyze(
            @RequestBody IncidentAnalysisRequestDTO request
    ) {
        log.info("Incident analysis request: {}", request);
        IncidentAnalysisResponseDTO response = service.analyze(request);
        return ResponseEntity.ok(response);
    }
}
