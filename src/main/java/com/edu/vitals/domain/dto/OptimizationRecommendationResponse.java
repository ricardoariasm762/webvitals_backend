package com.edu.vitals.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OptimizationRecommendationResponse {
    private float predictedInpMs;
    private String vitalsCategory;        // GOOD, NEEDS_IMPROVEMENT, POOR
    private String architecturalAdvice;    // Architectural technical recommendation
    private boolean offloadToWorker;       // true if Web Worker execution is recommended
    private boolean yieldMainThread;       // true if scheduler.yield() or requestIdleCallback is suggested
}
