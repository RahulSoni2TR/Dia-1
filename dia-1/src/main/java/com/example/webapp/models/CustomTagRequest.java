package com.example.webapp.models;

import java.util.List;

public class CustomTagRequest {

    private int startSlot; // 1-based from UI
    private List<String> designNos;

    // ✅ NEW
    private Float fontSize;

    public Float getFontSize() {
        return fontSize;
    }

    public void setFontSize(Float fontSize) {
        this.fontSize = fontSize;
    }
    
    public int getStartSlot() {
        return startSlot;
    }

    public void setStartSlot(int startSlot) {
        this.startSlot = startSlot;
    }

    public List<String> getDesignNos() {
        return designNos;
    }

    public void setDesignNos(List<String> designNos) {
        this.designNos = designNos;
    }
}

