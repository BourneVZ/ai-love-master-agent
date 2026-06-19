package com.bvz.aiagent.core.tool.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ImageSearchResultTest {

    @Test
    void shouldCarryImageItemsInStructuredForm() {
        ImageSearchResult.ImageItem item = new ImageSearchResult.ImageItem("Sunset", "https://img.example/a.jpg", "https://source.example/a");
        ImageSearchResult result = new ImageSearchResult(List.of(item));

        assertEquals(1, result.items().size());
        assertEquals("Sunset", result.items().getFirst().title());
        assertEquals("https://img.example/a.jpg", result.items().getFirst().imageUrl());
    }

    @Test
    void shouldRejectBlankImageUrl() {
        assertThrows(IllegalArgumentException.class, () -> new ImageSearchResult.ImageItem("title", " ", "source"));
    }
}
