package com.example.webapp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.example.webapp.models.Product;

public class BatchUpdateMapperTest {

    @Test
    void applyUpdates_setsExpectedFields() {
        Product product = new Product();
        Map<String, String> updates = new HashMap<>();
        updates.put("productName", "Test Item");
        updates.put("gross", "10.5");
        updates.put("productNet", "9.25");
        updates.put("pcs", "2");
        updates.put("diaRate", "1500");

        int applied = BatchUpdateMapper.applyUpdates(product, updates);

        assertEquals(5, applied);
        assertEquals("Test Item", product.getItem());
        assertEquals(new BigDecimal("10.5"), product.getGross());
        assertEquals(new BigDecimal("9.25"), product.getNet());
        assertEquals(2, product.getPcs());
        assertEquals(new BigDecimal("1500"), product.getDiaRt());
    }

    @Test
    void applyUpdates_rejectsUnknownField() {
        Product product = new Product();
        Map<String, String> updates = new HashMap<>();
        updates.put("unknownField", "1");

        assertThrows(IllegalArgumentException.class, () -> BatchUpdateMapper.applyUpdates(product, updates));
    }
}
