package com.bvz.aiagent.core.contract;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CoverageContractTest {

    @Test
    void shouldCarryCoverageConstraints() {
        CoverageContract contract = new CoverageContract("DATE_SPOTS", 5, 0.8, 2);

        assertEquals("DATE_SPOTS", contract.subjectType());
        assertEquals(5, contract.expectedCount());
        assertEquals(0.8, contract.minCoverageRatio());
        assertEquals(2, contract.perItemImageMinCount());
    }

    @Test
    void shouldRejectInvalidCoverageValues() {
        assertThrows(IllegalArgumentException.class, () -> new CoverageContract("DATE_SPOTS", 0, 0.8, 1));
        assertThrows(IllegalArgumentException.class, () -> new CoverageContract("DATE_SPOTS", 5, -0.1, 1));
        assertThrows(IllegalArgumentException.class, () -> new CoverageContract("DATE_SPOTS", 5, 1.1, 1));
        assertThrows(IllegalArgumentException.class, () -> new CoverageContract("DATE_SPOTS", 5, 0.8, -1));
    }
}
