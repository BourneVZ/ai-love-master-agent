package com.bvz.aiagent.core.tool.interpreter;

import com.bvz.aiagent.core.tool.model.WebSearchResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WebSearchResultInterpreterTest {

    @Test
    void shouldInterpretSearchWebRawResultIntoTopFiveStructuredItems() {
        String raw = "{\"title\":\"A\",\"snippet\":\"S1\",\"link\":\"https://example.com/a\"}," +
                "{\"title\":\"B\",\"snippet\":\"S2\",\"link\":\"https://example.com/b\"}";

        WebSearchResult result = new WebSearchResultInterpreter().interpret(raw);

        assertEquals(2, result.items().size());
        assertEquals("A", result.items().getFirst().title());
    }
}
