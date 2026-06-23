package com.bvz.aiagent.core.tool.interpreter;

import com.bvz.aiagent.core.tool.model.ImageSearchResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ImageSearchResultInterpreterTest {

    @Test
    void shouldInterpretSearchImageRawResultIntoStructuredItems() {
        String raw = "{\"title\":\"Cafe\",\"imageUrl\":\"https://img.example/cafe.jpg\",\"sourceUrl\":\"https://example.com/cafe\"}," +
                "{\"title\":\"Park\",\"imageUrl\":\"https://img.example/park.jpg\",\"sourceUrl\":\"https://example.com/park\"}";

        ImageSearchResult result = new ImageSearchResultInterpreter().interpret(raw);

        assertEquals(2, result.items().size());
        assertEquals("Cafe", result.items().getFirst().title());
    }
}
