package com.edu.vitals.service;

import com.edu.vitals.domain.PerformanceLog;
import com.edu.vitals.domain.dto.OptimizationRecommendationResponse;
import com.edu.vitals.domain.dto.PerformanceTelemetryRequest;
import com.edu.vitals.ml.InpInferenceEngine;
import com.edu.vitals.repository.PerformanceLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PerformanceAuditService {

    private final InpInferenceEngine inferenceEngine;
    private final PerformanceLogRepository performanceLogRepository;

    @Transactional
    public OptimizationRecommendationResponse auditPerformance(PerformanceTelemetryRequest request) {
        log.info("Processing telemetry for session [{}] on path [{}]", request.getSessionId(), request.getUrlPath());

        // 1. Predictive INP inference execution
        float predictedInp = inferenceEngine.predictInp(
                request.getLongTaskDurationMs(),
                request.getBlockingTimeMs(),
                request.getDomNodeCount(),
                request.getEventLoopLagMs()
        );

        // 2. Core Web Vitals threshold classification & UI technical advice
        String vitalsCategory;
        String architecturalAdvice;
        boolean offloadToWorker;
        boolean yieldMainThread;

        if (predictedInp <= 200.0f) {
            vitalsCategory = "GOOD";
            architecturalAdvice = "Presupuesto de fotogramas óptimo. Las tareas pueden continuar en el Main Thread sin degradación perceptible.";
            offloadToWorker = false;
            yieldMainThread = false;
        } else if (predictedInp <= 500.0f) {
            vitalsCategory = "NEEDS_IMPROVEMENT";
            architecturalAdvice = "Riesgo de retraso de renderizado. Fragmentar microtareas pesadas utilizando requestIdleCallback() o scheduler.yield().";
            offloadToWorker = false;
            yieldMainThread = true;
        } else {
            vitalsCategory = "POOR";
            architecturalAdvice = "Saturación crítica del Event Loop. Trasladar inmediatamente la ejecución del algoritmo intensivo a un Web Worker desacoplado.";
            offloadToWorker = true;
            yieldMainThread = true;
        }

        // 3. Telemetry persistence and prediction recording
        PerformanceLog entity = PerformanceLog.builder()
                .sessionId(request.getSessionId())
                .urlPath(request.getUrlPath())
                .longTaskDurationMs(request.getLongTaskDurationMs())
                .blockingTimeMs(request.getBlockingTimeMs())
                .domNodeCount(request.getDomNodeCount())
                .eventLoopLagMs(request.getEventLoopLagMs())
                .predictedInpMs(predictedInp)
                .vitalsStatus(vitalsCategory)
                .optimizationApplied(request.getOptimizationApplied() != null && request.getOptimizationApplied())
                .timestamp(LocalDateTime.now())
                .build();

        performanceLogRepository.save(entity);
        log.info("Performance log record persisted with ID [{}] - Predicted INP: {} ms ({})",
                entity.getId(), predictedInp, vitalsCategory);

        // 4. Return optimization prescription
        return OptimizationRecommendationResponse.builder()
                .predictedInpMs(predictedInp)
                .vitalsCategory(vitalsCategory)
                .architecturalAdvice(architecturalAdvice)
                .offloadToWorker(offloadToWorker)
                .yieldMainThread(yieldMainThread)
                .build();
    }

    @Transactional(readOnly = true)
    public List<PerformanceLog> getMetricsBySessionId(String sessionId) {
        log.debug("Querying historical metrics for session [{}]", sessionId);
        return performanceLogRepository.findBySessionIdOrderByTimestampAsc(sessionId);
    }
}
