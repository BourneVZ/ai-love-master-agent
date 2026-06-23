package com.bvz.aiagent.core.tool.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WebSearchResultTest {

    @Test
    void shouldCarryStructuredSearchItemsAndTopFiveContainer() {
        WebSearchResult.SearchItem item = new WebSearchResult.SearchItem("Title", "Snippet", "https://example.com");
        WebSearchResult result = new WebSearchResult(List.of(item));

        assertEquals(1, result.items().size());
        assertEquals("Title", result.items().getFirst().title());
        assertEquals("Snippet", result.items().getFirst().snippet());
    }

    @Test
    void shouldRejectBlankLink() {
        assertThrows(IllegalArgumentException.class, () -> new WebSearchResult.SearchItem("Title", "Snippet", " "));
    }
}
