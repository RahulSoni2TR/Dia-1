package com.example.webapp.models;

import java.util.Map;

public class BatchUpdateRequest {
    private Long categoryId;
    private Map<String, String> updates;

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Map<String, String> getUpdates() {
        return updates;
    }

    public void setUpdates(Map<String, String> updates) {
        this.updates = updates;
    }
}
