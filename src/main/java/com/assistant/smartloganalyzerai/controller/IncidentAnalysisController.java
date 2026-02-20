package com.assistant.smartloganalyzerai.controller;

import com.assistant.smartloganalyzerai.dto.api.IncidentAnalysisRequestDTO;
import com.assistant.smartloganalyzerai.dto.api.IncidentAnalysisResponseDTO;
import com.assistant.smartloganalyzerai.service.IncidentAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/incidents")
public class IncidentAnalysisController {
    private final IncidentAnalysisService service;

    @PostMapping("/analyze")
    public ResponseEntity<IncidentAnalysisResponseDTO> analyze(
            @RequestBody IncidentAnalysisRequestDTO request
    ) {
        IncidentAnalysisResponseDTO response = service.analyze(request);
        return ResponseEntity.ok(response);
    }
}
