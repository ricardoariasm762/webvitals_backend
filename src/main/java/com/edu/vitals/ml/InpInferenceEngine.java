package com.edu.vitals.ml;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

@Slf4j
@Component
public class InpInferenceEngine {

    @Value("${onnx.model.path:models/model_inp_predictor.onnx}")
    private String modelPath;

    private OrtEnvironment env;
    private OrtSession session;
    private String inputName;

    @PostConstruct
    public void init() {
        try {
            log.info("Initializing ONNX Runtime Environment...");
            this.env = OrtEnvironment.getEnvironment();

            log.info("Loading ONNX model from classpath: {}", modelPath);
            ClassPathResource resource = new ClassPathResource(modelPath);
            byte[] modelBytes;
            try (InputStream is = resource.getInputStream()) {
                modelBytes = is.readAllBytes();
            }

            OrtSession.SessionOptions options = new OrtSession.SessionOptions();
            this.session = env.createSession(modelBytes, options);

            if (!session.getInputNames().isEmpty()) {
                this.inputName = session.getInputNames().iterator().next();
                log.info("ONNX model loaded successfully. Input tensor name: {}", this.inputName);
            } else {
                this.inputName = "float_input";
                log.warn("No input name detected in ONNX model. Using default: {}", this.inputName);
            }
        } catch (Exception e) {
            log.error("Failed to initialize ONNX Runtime session with model [{}]. Fallback mode will be enabled.", modelPath, e);
        }
    }

    public float predictInp(float longTaskDurationMs, float blockingTimeMs, float domNodeCount, float eventLoopLagMs) {
        if (session != null && env != null) {
            try {
                float[][] inputData = new float[][] {
                    { longTaskDurationMs, blockingTimeMs, domNodeCount, eventLoopLagMs }
                };

                try (OnnxTensor inputTensor = OnnxTensor.createTensor(env, inputData)) {
                    Map<String, OnnxTensor> inputs = Collections.singletonMap(inputName, inputTensor);
                    try (OrtSession.Result results = session.run(inputs)) {
                        Object outputVal = results.get(0).getValue();
                        return extractPredictedValue(outputVal);
                    }
                }
            } catch (Exception e) {
                log.warn("Error during ONNX inference. Applying heuristic mathematical fallback.", e);
            }
        } else {
            log.warn("ONNX session not available. Running estimation via heuristic fallback.");
        }

        return calculateHeuristicFallback(longTaskDurationMs, blockingTimeMs, domNodeCount, eventLoopLagMs);
    }

    private float extractPredictedValue(Object outputVal) {
        if (outputVal instanceof float[][] matrix) {
            return matrix[0][0];
        } else if (outputVal instanceof float[] array) {
            return array[0];
        } else if (outputVal instanceof float[][][] cube) {
            return cube[0][0][0];
        } else if (outputVal instanceof Number number) {
            return number.floatValue();
        } else {
            throw new IllegalArgumentException("Unrecognized ONNX output format: " + outputVal.getClass().getName());
        }
    }

    private float calculateHeuristicFallback(float longTaskDurationMs, float blockingTimeMs, float domNodeCount, float eventLoopLagMs) {
        float domPenalty = (domNodeCount / 1000.0f) * 8.5f;
        float estimatedInp = 16.6f
            + (0.75f * longTaskDurationMs)
            + (0.45f * eventLoopLagMs)
            + domPenalty;
        return Math.max(16.6f, Math.min(estimatedInp, 900.0f));
    }

    @PreDestroy
    public void cleanup() {
        log.info("Releasing ONNX Runtime resources...");
        try {
            if (session != null) {
                session.close();
            }
        } catch (OrtException e) {
            log.error("Error closing OrtSession", e);
        }
        try {
            if (env != null) {
                env.close();
            }
        } catch (Exception e) {
            log.error("Error closing OrtEnvironment", e);
        }
    }
}
