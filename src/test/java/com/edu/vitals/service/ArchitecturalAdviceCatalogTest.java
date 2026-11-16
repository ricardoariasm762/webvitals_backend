package com.edu.vitals.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArchitecturalAdviceCatalogTest {

    private ArchitecturalAdviceCatalog catalog;

    @BeforeEach
    void setUp() {
        catalog = new ArchitecturalAdviceCatalog();
    }

    @Test
    void testResolveAdviceGood() {
        String advice = catalog.resolveAdvice("GOOD");
        assertEquals(ArchitecturalAdviceCatalog.GOOD_ADVICE, advice);
        assertTrue(advice.contains("Presupuesto de fotogramas"));
    }

    @Test
    void testResolveAdviceNeedsImprovement() {
        String advice = catalog.resolveAdvice("NEEDS_IMPROVEMENT");
        assertEquals(ArchitecturalAdviceCatalog.NEEDS_IMPROVEMENT_ADVICE, advice);
        assertTrue(advice.contains("scheduler.yield()"));
    }

    @Test
    void testResolveAdvicePoor() {
        String advice = catalog.resolveAdvice("POOR");
        assertEquals(ArchitecturalAdviceCatalog.POOR_ADVICE, advice);
        assertTrue(advice.contains("Web Worker desacoplado"));
    }
}

