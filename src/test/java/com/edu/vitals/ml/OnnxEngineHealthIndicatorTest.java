package com.edu.vitals.ml;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class OnnxEngineHealthIndicatorTest {

    @Mock
    private InpInferenceEngine inferenceEngine;

    @Test
    void testHealthDetails() {
        OnnxEngineHealthIndicator indicator = new OnnxEngineHealthIndicator(inferenceEngine);
        Map<String, Object> details = indicator.getHealthDetails();

        assertNotNull(details);
        assertEquals("UP", details.get("status"));
        assertEquals("ONNX Runtime Java", details.get("engine"));
    }
}
