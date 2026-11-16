package com.edu.vitals.validation;

import com.edu.vitals.domain.dto.PerformanceTelemetryRequest;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Validates incoming telemetry metrics against realistic browser runtime bounds.
 */
@Component
public class TelemetryPayloadValidator {

    public static final float MAX_LONG_TASK_MS = 60_000.0f; // 1 minute
    public static final float MAX_BLOCKING_TIME_MS = 60_000.0f;
    public static final float MAX_DOM_NODES = 200_000.0f;
    public static final float MAX_EVENT_LOOP_LAG_MS = 30_000.0f;

    public List<String> validate(PerformanceTelemetryRequest request) {
        List<String> errors = new ArrayList<>();

        if (request == null) {
            errors.add("Telemetry payload must not be null");
            return errors;
        }

        if (request.getSessionId() == null || request.getSessionId().trim().isEmpty()) {
            errors.add("Session ID is required");
        }

        if (request.getLongTaskDurationMs() < 0 || request.getLongTaskDurationMs() > MAX_LONG_TASK_MS) {
            errors.add("Long task duration must be between 0 and " + MAX_LONG_TASK_MS + " ms");
        }

        if (request.getBlockingTimeMs() < 0 || request.getBlockingTimeMs() > MAX_BLOCKING_TIME_MS) {
            errors.add("Blocking time must be between 0 and " + MAX_BLOCKING_TIME_MS + " ms");
        }

        if (request.getDomNodeCount() < 0 || request.getDomNodeCount() > MAX_DOM_NODES) {
            errors.add("DOM node count must be between 0 and " + MAX_DOM_NODES);
        }

        if (request.getEventLoopLagMs() < 0 || request.getEventLoopLagMs() > MAX_EVENT_LOOP_LAG_MS) {
            errors.add("Event loop lag must be between 0 and " + MAX_EVENT_LOOP_LAG_MS + " ms");
        }

        return errors;
    }
}

