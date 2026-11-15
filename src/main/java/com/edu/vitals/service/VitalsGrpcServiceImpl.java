package com.edu.vitals.service;

import com.edu.vitals.domain.dto.OptimizationRecommendationResponse;
import com.edu.vitals.domain.dto.PerformanceTelemetryRequest;
import com.edu.vitals.grpc.OptimizationVerdict;
import com.edu.vitals.grpc.StreamSummary;
import com.edu.vitals.grpc.TelemetryFrame;
import com.edu.vitals.grpc.VitalsAuditorServiceGrpc;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class VitalsGrpcServiceImpl extends VitalsAuditorServiceGrpc.VitalsAuditorServiceImplBase {

    private final PerformanceAuditService performanceAuditService;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void evaluateSingleFrame(TelemetryFrame request, StreamObserver<OptimizationVerdict> responseObserver) {
        log.info("gRPC EvaluateSingleFrame received for session [{}] on [{}]", request.getSessionId(), request.getUrlPath());

        try {
            // 1. Map binary frame to internal DTO
            PerformanceTelemetryRequest requestDto = mapFrameToDto(request);

            // 2. Execute ONNX inference and persist
            OptimizationRecommendationResponse verdictDto = performanceAuditService.auditPerformance(requestDto);

            // 3. Real-time Fan-Out via WebSocket STOMP broker
            publishToWebSocket(request.getSessionId(), verdictDto);

            // 4. Return binary gRPC verdict
            OptimizationVerdict verdict = mapDtoToVerdict(verdictDto);
            responseObserver.onNext(verdict);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error processing gRPC EvaluateSingleFrame: {}", e.getMessage(), e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Error processing telemetry frame: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public StreamObserver<TelemetryFrame> streamTelemetry(StreamObserver<StreamSummary> responseObserver) {
        log.info("gRPC StreamTelemetry session started");

        return new StreamObserver<TelemetryFrame>() {
            private int framesProcessed = 0;
            private double totalPredictedInp = 0.0;

            @Override
            public void onNext(TelemetryFrame frame) {
                framesProcessed++;

                try {
                    PerformanceTelemetryRequest requestDto = mapFrameToDto(frame);
                    OptimizationRecommendationResponse verdictDto = performanceAuditService.auditPerformance(requestDto);
                    totalPredictedInp += verdictDto.getPredictedInpMs();

                    // Real-time Fan-Out to WebSockets for each incoming frame
                    publishToWebSocket(frame.getSessionId(), verdictDto);
                } catch (Exception e) {
                    log.error("Error processing frame [{}] in StreamTelemetry: {}", framesProcessed, e.getMessage());
                }
            }

            @Override
            public void onError(Throwable t) {
                log.error("gRPC StreamTelemetry stream cancelled or failed: {}", t.getMessage());
            }

            @Override
            public void onCompleted() {
                float avgInp = framesProcessed > 0 ? (float) (totalPredictedInp / framesProcessed) : 0.0f;
                log.info("gRPC StreamTelemetry completed. Processed frames: {}, Average INP: {} ms", framesProcessed, avgInp);

                StreamSummary summary = StreamSummary.newBuilder()
                        .setFramesProcessed(framesProcessed)
                        .setAveragePredictedInp(avgInp)
                        .build();

                responseObserver.onNext(summary);
                responseObserver.onCompleted();
            }
        };
    }

    private PerformanceTelemetryRequest mapFrameToDto(TelemetryFrame frame) {
        return PerformanceTelemetryRequest.builder()
                .sessionId(frame.getSessionId())
                .urlPath(frame.getUrlPath().isEmpty() ? "/" : frame.getUrlPath())
                .longTaskDurationMs(frame.getLongTaskDurationMs())
                .blockingTimeMs(frame.getBlockingTimeMs())
                .domNodeCount(frame.getDomNodeCount())
                .eventLoopLagMs(frame.getEventLoopLagMs())
                .optimizationApplied(false)
                .build();
    }

    private OptimizationVerdict mapDtoToVerdict(OptimizationRecommendationResponse dto) {
        return OptimizationVerdict.newBuilder()
                .setPredictedInpMs(dto.getPredictedInpMs())
                .setVitalsCategory(dto.getVitalsCategory())
                .setArchitecturalAdvice(dto.getArchitecturalAdvice())
                .setOffloadToWorker(dto.isOffloadToWorker())
                .setYieldMainThread(dto.isYieldMainThread())
                .build();
    }

    private void publishToWebSocket(String sessionId, OptimizationRecommendationResponse verdictDto) {
        if (sessionId == null || sessionId.isBlank()) {
            sessionId = "default";
        }
        String destination = "/topic/vitals/" + sessionId;
        log.debug("WebSocket fan-out -> channel [{}] with INP: {} ms", destination, verdictDto.getPredictedInpMs());
        messagingTemplate.convertAndSend(destination, verdictDto);
    }
}
