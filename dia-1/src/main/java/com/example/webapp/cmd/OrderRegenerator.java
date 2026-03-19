package com.example.webapp.cmd;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.webapp.models.Orders;
import com.example.webapp.repository.OrderRepository;

//@Component
//@Order(1)
public class OrderRegenerator implements CommandLineRunner {

    @Autowired
    private OrderRepository ordersRepository;

    // Old prefixes: only delete these if unassigned
    private static final Set<String> OLD_PREFIXES = new HashSet<>(Arrays.asList(
        "DIA-", "OS-", "PG-", "VIL-", "JAD-"
    ));

    /**
     * Holds prefix + start + end for each subCategoryId
     */
    private static final Map<Integer, OrderRange> SUBCATEGORY_CONFIGS = new HashMap<>();

    /**
     * Simple holder for order ID ranges
     */
    private static class OrderRange {
        String prefix;
        int start;
        int end;

        OrderRange(String prefix, int start, int end) {
            this.prefix = prefix;
            this.start = start;
            this.end = end;
        }
    }

    static {
        // ---------------- Jadtar ----------------
        SUBCATEGORY_CONFIGS.put(7,  new OrderRange("JHS", 1, 200));  // Halfsets
        SUBCATEGORY_CONFIGS.put(8,  new OrderRange("JB",  1, 200));  // Bangles
        SUBCATEGORY_CONFIGS.put(6,  new OrderRange("JR",  1, 200));  // Rings
        SUBCATEGORY_CONFIGS.put(17, new OrderRange("JE",  1, 200));  // Earrings

        // ---------------- Vilandi (FIXED RANGES) ----------------
        SUBCATEGORY_CONFIGS.put(9,  new OrderRange("VHS", 301, 400)); // Halfsets
        SUBCATEGORY_CONFIGS.put(10, new OrderRange("VB",  201, 300)); // Bangles

        // ---------------- Open Setting ----------------
        SUBCATEGORY_CONFIGS.put(14, new OrderRange("OHS", 1, 200)); // Halfsets
        SUBCATEGORY_CONFIGS.put(15, new OrderRange("OSB", 1, 200)); // Bangles

        // ---------------- Plain Gold ----------------
        SUBCATEGORY_CONFIGS.put(16, new OrderRange("GB", 1, 200));  // Bangles
        SUBCATEGORY_CONFIGS.put(18, new OrderRange("GC", 1, 200));  // Chains

        // ---------------- Diamond ----------------
        SUBCATEGORY_CONFIGS.put(13, new OrderRange("DHS", 1, 200)); // Halfsets
        SUBCATEGORY_CONFIGS.put(11, new OrderRange("DB",  1, 200)); // Bangles
        SUBCATEGORY_CONFIGS.put(20, new OrderRange("DR",  1, 200)); // Rings
        SUBCATEGORY_CONFIGS.put(19, new OrderRange("DE",  1, 200)); // Earrings
        SUBCATEGORY_CONFIGS.put(12, new OrderRange("DPS", 1, 200)); // Pendant Sets
    }

    @Override
    @Transactional
    public void run(String... args) {
        System.out.println("🔄 Migrating orders to new category-based IDs...");

        // Step 1: Delete only OLD unassigned orders
        int deletedCount = deleteOldUnassignedOrders();
        System.out.printf("🗑️  Deleted %d old unassigned orders%n", deletedCount);

        System.out.println("🔄 Generating orders by ACTUAL subCategoryId...");

        int totalInserted = 0;

        for (Map.Entry<Integer, OrderRange> entry : SUBCATEGORY_CONFIGS.entrySet()) {
            int subCategoryId = entry.getKey();
            OrderRange range = entry.getValue();

            totalInserted += generateOrdersForSubCategory(
                subCategoryId,
                range.prefix,
                range.start,
                range.end
            );
        }

        System.out.printf("✅ Complete: %d new orders added%n", totalInserted);
    }

    private int generateOrdersForSubCategory(
            int subCategoryId,
            String prefix,
            int start,
            int end
    ) {
        int inserted = 0;

        for (int i = start; i <= end; i++) {
            String orderId = prefix + i; // e.g. VHS301, VB201

            // Global uniqueness check
            if (ordersRepository.existsByOrderId(orderId)) {
                continue;
            }

            Orders order = new Orders();
            order.setOrderId(orderId);
            order.setCategoryId(subCategoryId); // ACTUAL subCategoryId
            order.setAssigned(false);
            order.setAssignedProduct(null);

            try {
                ordersRepository.save(order);
                inserted++;
            } catch (DataIntegrityViolationException ex) {
                System.err.printf("⚠️ Skip duplicate: %s%n", orderId);
            }
        }

        System.out.printf(
            "  ✅ SubCat %d %s: %d inserted (%d–%d)%n",
            subCategoryId, prefix, inserted, start, end
        );

        return inserted;
    }

    private int deleteOldUnassignedOrders() {
        return ordersRepository.deleteOldUnassignedOrders();
    }
}
