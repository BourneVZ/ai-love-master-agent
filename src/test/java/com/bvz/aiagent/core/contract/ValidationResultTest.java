package com.bvz.aiagent.core.contract;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidationResultTest {

    @Test
    void shouldCarryValidationOutcomeIssuesAndSummary() {
        ValidationResult result = new ValidationResult(
                false,
                List.of("缺少外部证据", "PDF 不可读"),
                true,
                "仍可通过补充搜索和重新生成产物修复"
        );

        assertTrue(result.issues().contains("缺少外部证据"));
        assertTrue(result.issues().contains("PDF 不可读"));
        assertEquals(false, result.passed());
        assertEquals(true, result.repairable());
        assertEquals("仍可通过补充搜索和重新生成产物修复", result.summary());
    }

    @Test
    void shouldRejectFailedResultWithoutIssues() {
        assertThrows(IllegalArgumentException.class, () -> new ValidationResult(
                false,
                List.of(),
                true,
                "失败但没有问题列表"
        ));
    }
}
