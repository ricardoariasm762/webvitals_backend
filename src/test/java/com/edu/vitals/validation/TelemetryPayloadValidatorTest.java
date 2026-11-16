package com.edu.vitals.validation;

import com.edu.vitals.domain.dto.PerformanceTelemetryRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TelemetryPayloadValidatorTest {

    private TelemetryPayloadValidator validator;

    @BeforeEach
    void setUp() {
        validator = new TelemetryPayloadValidator();
    }

    @Test
    void testValidPayload() {
        PerformanceTelemetryRequest validRequest = PerformanceTelemetryRequest.builder()
                .sessionId("session-val-1")
                .urlPath("/home")
                .longTaskDurationMs(80.0f)
                .blockingTimeMs(30.0f)
                .domNodeCount(1200.0f)
                .eventLoopLagMs(12.0f)
                .build();

        List<String> errors = validator.validate(validRequest);
        assertTrue(errors.isEmpty(), "Valid request should produce no validation errors");
    }

    @Test
    void testInvalidPayloadBoundaries() {
        PerformanceTelemetryRequest invalidRequest = PerformanceTelemetryRequest.builder()
                .sessionId("")
                .longTaskDurationMs(-10.0f)
                .blockingTimeMs(-5.0f)
                .domNodeCount(-100.0f)
                .eventLoopLagMs(-1.0f)
                .build();

        List<String> errors = validator.validate(invalidRequest);
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(e -> e.contains("Session ID")));
        assertTrue(errors.stream().anyMatch(e -> e.contains("Long task")));
    }
}

