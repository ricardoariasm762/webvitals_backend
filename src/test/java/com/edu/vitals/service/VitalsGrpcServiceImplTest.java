package com.edu.vitals.service;

import com.edu.vitals.domain.dto.OptimizationRecommendationResponse;
import com.edu.vitals.domain.dto.PerformanceTelemetryRequest;
import com.edu.vitals.grpc.OptimizationVerdict;
import com.edu.vitals.grpc.StreamSummary;
import com.edu.vitals.grpc.TelemetryFrame;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VitalsGrpcServiceImplTest {

    @Mock
    private PerformanceAuditService performanceAuditService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private VitalsGrpcServiceImpl vitalsGrpcService;

    @Test
    void testEvaluateSingleFrame_Success() {
        TelemetryFrame frame = TelemetryFrame.newBuilder()
                .setSessionId("session-xyz")
                .setUrlPath("/dashboard")
                .setLongTaskDurationMs(75.5f)
                .setBlockingTimeMs(25.5f)
                .setDomNodeCount(1200.0f)
                .setEventLoopLagMs(18.2f)
                .setTimestampEpoch(System.currentTimeMillis())
                .build();

        OptimizationRecommendationResponse expectedVerdict = OptimizationRecommendationResponse.builder()
                .predictedInpMs(230.5f)
                .vitalsCategory("NEEDS_IMPROVEMENT")
                .architecturalAdvice("Fragment microtasks using scheduler.yield()")
                .offloadToWorker(false)
                .yieldMainThread(true)
                .build();

        when(performanceAuditService.auditPerformance(any(PerformanceTelemetryRequest.class)))
                .thenReturn(expectedVerdict);

        @SuppressWarnings("unchecked")
        StreamObserver<OptimizationVerdict> responseObserver = mock(StreamObserver.class);

        vitalsGrpcService.evaluateSingleFrame(frame, responseObserver);

        // Verify WebSocket Fan-Out
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/vitals/session-xyz"), eq(expectedVerdict));

        // Verify binary gRPC response
        ArgumentCaptor<OptimizationVerdict> verdictCaptor = ArgumentCaptor.forClass(OptimizationVerdict.class);
        verify(responseObserver, times(1)).onNext(verdictCaptor.capture());
        verify(responseObserver, times(1)).onCompleted();

        OptimizationVerdict verdict = verdictCaptor.getValue();
        assertEquals(230.5f, verdict.getPredictedInpMs());
        assertEquals("NEEDS_IMPROVEMENT", verdict.getVitalsCategory());
        assertTrue(verdict.getYieldMainThread());
        assertFalse(verdict.getOffloadToWorker());
    }

    @Test
    void testStreamTelemetry_Success() {
        OptimizationRecommendationResponse verdict1 = OptimizationRecommendationResponse.builder()
                .predictedInpMs(100.0f)
                .vitalsCategory("GOOD")
                .architecturalAdvice("Nominal")
                .offloadToWorker(false)
                .yieldMainThread(false)
                .build();

        OptimizationRecommendationResponse verdict2 = OptimizationRecommendationResponse.builder()
                .predictedInpMs(300.0f)
                .vitalsCategory("NEEDS_IMPROVEMENT")
                .architecturalAdvice("Yield")
                .offloadToWorker(false)
                .yieldMainThread(true)
                .build();

        when(performanceAuditService.auditPerformance(any(PerformanceTelemetryRequest.class)))
                .thenReturn(verdict1, verdict2);

        @SuppressWarnings("unchecked")
        StreamObserver<StreamSummary> responseObserver = mock(StreamObserver.class);

        StreamObserver<TelemetryFrame> requestObserver = vitalsGrpcService.streamTelemetry(responseObserver);

        TelemetryFrame frame1 = TelemetryFrame.newBuilder()
                .setSessionId("sess-stream")
                .setUrlPath("/stream-1")
                .setLongTaskDurationMs(30.0f)
                .build();

        TelemetryFrame frame2 = TelemetryFrame.newBuilder()
                .setSessionId("sess-stream")
                .setUrlPath("/stream-2")
                .setLongTaskDurationMs(90.0f)
                .build();

        requestObserver.onNext(frame1);
        requestObserver.onNext(frame2);
        requestObserver.onCompleted();

        verify(messagingTemplate, times(2)).convertAndSend(eq("/topic/vitals/sess-stream"), any(OptimizationRecommendationResponse.class));

        ArgumentCaptor<StreamSummary> summaryCaptor = ArgumentCaptor.forClass(StreamSummary.class);
        verify(responseObserver, times(1)).onNext(summaryCaptor.capture());
        verify(responseObserver, times(1)).onCompleted();

        StreamSummary summary = summaryCaptor.getValue();
        assertEquals(2, summary.getFramesProcessed());
        assertEquals(200.0f, summary.getAveragePredictedInp(), 0.01f);
    }
}
