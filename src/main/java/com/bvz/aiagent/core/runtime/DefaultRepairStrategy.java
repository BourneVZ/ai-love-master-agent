package com.bvz.aiagent.core.runtime;

import com.bvz.aiagent.core.contract.ValidationResult;
import com.bvz.aiagent.core.model.ExecutionState;
import com.bvz.aiagent.core.model.RepairInstruction;

public class DefaultRepairStrategy {

    public RepairInstruction buildRepairInstruction(ValidationResult result, ExecutionState state) {
        String prompt = "Please address the following validation issues without changing the task goal: "
                + String.join("; ", result.issues());
        return new RepairInstruction(prompt, result.issues(), result.repairable());
    }
}
