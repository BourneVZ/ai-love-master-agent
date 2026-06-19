package com.bvz.aiagent.core.tool.model;

import java.util.List;

public record WebSearchResult(List<SearchItem> items) {

    public WebSearchResult {
        items = List.copyOf(items);
    }

    public record SearchItem(String title, String snippet, String link) {
        public SearchItem {
            if (link == null || link.isBlank()) {
                throw new IllegalArgumentException("link must not be blank");
            }
            title = title == null ? "" : title;
            snippet = snippet == null ? "" : snippet;
        }
    }
}
