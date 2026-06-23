package com.bvz.aiagent.eval;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoveManusEvaluationMatrixTest {

    @Test
    void shouldExposeScenarioCatalogCoveringDatePlanAdviceAndMessageDraft() throws Exception {
        Class<?> catalogClass = Class.forName("com.bvz.aiagent.eval.LoveTaskScenarioCatalog");
        Object catalog = catalogClass.getDeclaredConstructor().newInstance();
        Method scenariosMethod = catalogClass.getMethod("scenarios");

        Object scenariosObject = scenariosMethod.invoke(catalog);
        assertTrue(scenariosObject instanceof List<?>);

        List<?> scenarios = (List<?>) scenariosObject;
        assertEquals(3, scenarios.size());
    }
}
