package com.edu.vitals.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Aggregated telemetry summary for an active audit session.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionSummaryDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private String sessionId;
    private int totalFrames;
    private float averageInpMs;
    private float peakInpMs;
    private float averageEventLoopLagMs;
    private float totalBlockingTimeMs;
    private String predominantCategory;
    private boolean workerOffloadRecommended;
}
