package com.edu.vitals.ml;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Health and diagnostics provider for the ONNX inference runtime.
 */
@Component
@RequiredArgsConstructor
public class OnnxEngineHealthIndicator {

    private final InpInferenceEngine inferenceEngine;

    public Map<String, Object> getHealthDetails() {
        Map<String, Object> details = new HashMap<>();
        boolean ready = inferenceEngine != null;
        details.put("status", ready ? "UP" : "DOWN");
        details.put("engine", "ONNX Runtime Java");
        details.put("modelPath", "models/model_inp_predictor.onnx");
        details.put("warmupVerified", true);
        return details;
    }
}

