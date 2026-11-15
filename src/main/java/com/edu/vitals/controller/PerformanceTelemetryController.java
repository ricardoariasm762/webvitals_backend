package com.edu.vitals.controller;

import com.edu.vitals.domain.PerformanceLog;
import com.edu.vitals.domain.dto.OptimizationRecommendationResponse;
import com.edu.vitals.domain.dto.PerformanceTelemetryRequest;
import com.edu.vitals.domain.dto.SessionSummaryDto;
import com.edu.vitals.service.PerformanceAuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/vitals")
@CrossOrigin(origins = {"http://localhost:4200", "http://127.0.0.1:4200"}, allowedHeaders = "*", allowCredentials = "true")
@RequiredArgsConstructor
public class PerformanceTelemetryController {

    private final PerformanceAuditService performanceAuditService;

    @PostMapping("/evaluate")
    public ResponseEntity<OptimizationRecommendationResponse> evaluateTelemetry(
            @RequestBody PerformanceTelemetryRequest request) {
        log.info("Received telemetry evaluation request for session: {}", request.getSessionId());
        OptimizationRecommendationResponse response = performanceAuditService.auditPerformance(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/metrics/session/{sessionId}")
    public ResponseEntity<List<PerformanceLog>> getSessionMetrics(
            @PathVariable String sessionId) {
        log.info("Received query for historical metrics for session: {}", sessionId);
        List<PerformanceLog> metrics = performanceAuditService.getMetricsBySessionId(sessionId);
        return ResponseEntity.ok(metrics);
    }

    @GetMapping("/metrics/session/{sessionId}/summary")
    public ResponseEntity<SessionSummaryDto> getSessionSummary(
            @PathVariable String sessionId) {
        log.info("Received query for session telemetry summary: {}", sessionId);
        SessionSummaryDto summary = performanceAuditService.getSessionSummary(sessionId);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "UP");
        status.put("service", "Event Loop Telemetry & Predictive INP Optimization System");
        status.put("timestamp", Instant.now().toString());
        return ResponseEntity.ok(status);
    }
}
