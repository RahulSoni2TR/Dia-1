package com.example.webapp.models;

public class TagProductRef {

    private Long productId;
    private String designNo; // optional, but useful

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getDesignNo() {
        return designNo;
    }

    public void setDesignNo(String designNo) {
        this.designNo = designNo;
    }
}
