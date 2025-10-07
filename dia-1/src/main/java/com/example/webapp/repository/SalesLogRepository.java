package com.example.webapp.repository;

import com.example.webapp.entity.SalesLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SalesLogRepository extends JpaRepository<SalesLog, Long> {

    @Query(
        value = "SELECT * FROM sales_log e " +
                "WHERE (:q IS NULL OR LOWER(e.design_no) LIKE CONCAT('%', LOWER(:q), '%') " +
                "OR LOWER(e.customer_name) LIKE CONCAT('%', LOWER(:q), '%') " +
                "OR LOWER(e.order_id) LIKE CONCAT('%', LOWER(:q), '%')) " +
                "AND (:categoryIdStr IS NULL OR JSON_UNQUOTE(JSON_EXTRACT(e.product_snapshot, '$.categoryId')) = :categoryIdStr) " +
                "AND (:subCategoryIdStr IS NULL OR JSON_UNQUOTE(JSON_EXTRACT(e.product_snapshot, '$.subCategoryId')) = :subCategoryIdStr) " +
                "ORDER BY e.created_at DESC",
        countQuery = "SELECT count(*) FROM sales_log e " +
                "WHERE (:q IS NULL OR LOWER(e.design_no) LIKE CONCAT('%', LOWER(:q), '%') " +
                "OR LOWER(e.customer_name) LIKE CONCAT('%', LOWER(:q), '%') " +
                "OR LOWER(e.order_id) LIKE CONCAT('%', LOWER(:q), '%')) " +
                "AND (:categoryIdStr IS NULL OR JSON_UNQUOTE(JSON_EXTRACT(e.product_snapshot, '$.categoryId')) = :categoryIdStr) " +
                "AND (:subCategoryIdStr IS NULL OR JSON_UNQUOTE(JSON_EXTRACT(e.product_snapshot, '$.subCategoryId')) = :subCategoryIdStr)",
        nativeQuery = true
    )
    Page<SalesLog> searchWithJsonFilters(
            @Param("q") String q,
            @Param("categoryIdStr") String categoryIdStr,
            @Param("subCategoryIdStr") String subCategoryIdStr,
            Pageable pageable
    );
}
