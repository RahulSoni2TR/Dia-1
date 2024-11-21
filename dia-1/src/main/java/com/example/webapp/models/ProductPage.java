package com.example.webapp.models;

import java.util.List;

public class ProductPage {
        private List<Product> products;
        private long totalCount;

        public ProductPage(List<Product> products, long totalCount) {
            this.products = products;
            this.totalCount = totalCount;
        }

        public List<Product> getProducts() {
            return products;
        }

        public long getTotalCount() {
            return totalCount;
        }
    }