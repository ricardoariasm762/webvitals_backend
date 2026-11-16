package com.edu.vitals.service;

import org.springframework.stereotype.Component;

/**
 * Catalog providing contextual architectural prescriptions based on predicted INP and vitals category.
 */
@Component
public class ArchitecturalAdviceCatalog {

    public static final String GOOD_ADVICE = "Presupuesto de fotogramas óptimo. Las tareas pueden continuar en el Main Thread sin degradación perceptible.";
    public static final String NEEDS_IMPROVEMENT_ADVICE = "Riesgo de retraso de renderizado. Fragmentar microtareas pesadas utilizando requestIdleCallback() o scheduler.yield().";
    public static final String POOR_ADVICE = "Saturación crítica del Event Loop. Trasladar inmediatamente la ejecución del algoritmo intensivo a un Web Worker desacoplado.";

    public String resolveAdvice(String category) {
        if ("GOOD".equalsIgnoreCase(category)) {
            return GOOD_ADVICE;
        } else if ("NEEDS_IMPROVEMENT".equalsIgnoreCase(category)) {
            return NEEDS_IMPROVEMENT_ADVICE;
        } else {
            return POOR_ADVICE;
        }
    }
}

