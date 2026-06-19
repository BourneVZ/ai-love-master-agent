package com.bvz.aiagent.core.tool.model;

import java.util.List;

public record ImageSearchResult(List<ImageItem> items) {

    public ImageSearchResult {
        items = List.copyOf(items);
    }

    public record ImageItem(String title, String imageUrl, String sourceUrl) {
        public ImageItem {
            if (imageUrl == null || imageUrl.isBlank()) {
                throw new IllegalArgumentException("imageUrl must not be blank");
            }
            title = title == null ? "" : title;
            sourceUrl = sourceUrl == null ? "" : sourceUrl;
        }
    }
}
