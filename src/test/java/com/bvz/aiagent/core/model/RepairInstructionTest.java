package com.bvz.aiagent.core.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RepairInstructionTest {

    @Test
    void shouldCarryRepairPromptReasonsAndRetryFlag() {
        RepairInstruction instruction = new RepairInstruction(
                "请补充外部证据并重新生成 PDF",
                List.of("缺少外部证据", "PDF 不可读"),
                true
        );

        assertEquals("请补充外部证据并重新生成 PDF", instruction.prompt());
        assertEquals(List.of("缺少外部证据", "PDF 不可读"), instruction.reasons());
        assertTrue(instruction.allowRetry());
    }

    @Test
    void shouldRejectBlankPromptOrEmptyReasons() {
        assertThrows(IllegalArgumentException.class, () -> new RepairInstruction(" ", List.of("reason"), true));
        assertThrows(IllegalArgumentException.class, () -> new RepairInstruction("repair", List.of(), true));
    }
}
