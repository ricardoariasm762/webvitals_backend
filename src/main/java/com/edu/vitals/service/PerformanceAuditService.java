package com.edu.vitals.service;

import com.edu.vitals.domain.PerformanceLog;
import com.edu.vitals.domain.dto.OptimizationRecommendationResponse;
import com.edu.vitals.domain.dto.PerformanceTelemetryRequest;
import com.edu.vitals.domain.dto.SessionSummaryDto;
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
    private final ArchitecturalAdviceCatalog adviceCatalog;

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
        boolean offloadToWorker;
        boolean yieldMainThread;

        if (predictedInp <= 200.0f) {
            vitalsCategory = "GOOD";
            offloadToWorker = false;
            yieldMainThread = false;
        } else if (predictedInp <= 500.0f) {
            vitalsCategory = "NEEDS_IMPROVEMENT";
            offloadToWorker = false;
            yieldMainThread = true;
        } else {
            vitalsCategory = "POOR";
            offloadToWorker = true;
            yieldMainThread = true;
        }

        String architecturalAdvice = adviceCatalog != null ?
                adviceCatalog.resolveAdvice(vitalsCategory) :
                (vitalsCategory.equals("GOOD") ? ArchitecturalAdviceCatalog.GOOD_ADVICE :
                        vitalsCategory.equals("NEEDS_IMPROVEMENT") ? ArchitecturalAdviceCatalog.NEEDS_IMPROVEMENT_ADVICE :
                                ArchitecturalAdviceCatalog.POOR_ADVICE);

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

    @Transactional(readOnly = true)
    public SessionSummaryDto getSessionSummary(String sessionId) {
        List<PerformanceLog> logs = getMetricsBySessionId(sessionId);
        if (logs.isEmpty()) {
            return SessionSummaryDto.builder()
                    .sessionId(sessionId)
                    .totalFrames(0)
                    .averageInpMs(0f)
                    .peakInpMs(0f)
                    .averageEventLoopLagMs(0f)
                    .totalBlockingTimeMs(0f)
                    .predominantCategory("UNKNOWN")
                    .workerOffloadRecommended(false)
                    .build();
        }

        int count = logs.size();
        float sumInp = 0f;
        float peakInp = 0f;
        float sumLag = 0f;
        float sumBlocking = 0f;
        int poorCount = 0;

        for (PerformanceLog p : logs) {
            float inp = p.getPredictedInpMs() != null ? p.getPredictedInpMs() : 0f;
            sumInp += inp;
            if (inp > peakInp) peakInp = inp;
            if (p.getEventLoopLagMs() != null) sumLag += p.getEventLoopLagMs();
            if (p.getBlockingTimeMs() != null) sumBlocking += p.getBlockingTimeMs();
            if ("POOR".equalsIgnoreCase(p.getVitalsStatus())) poorCount++;
        }

        float avgInp = sumInp / count;
        float avgLag = sumLag / count;
        String predominant = avgInp <= 200f ? "GOOD" : avgInp <= 500f ? "NEEDS_IMPROVEMENT" : "POOR";

        return SessionSummaryDto.builder()
                .sessionId(sessionId)
                .totalFrames(count)
                .averageInpMs(avgInp)
                .peakInpMs(peakInp)
                .averageEventLoopLagMs(avgLag)
                .totalBlockingTimeMs(sumBlocking)
                .predominantCategory(predominant)
                .workerOffloadRecommended(poorCount > 0 || avgInp > 500f)
                .build();
    }
}
