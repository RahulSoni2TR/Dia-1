package com.example.webapp.repository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.webapp.entity.EnquiryLog;

public interface EnquiryLogRepository extends JpaRepository<EnquiryLog, Long> {
	
	Page<EnquiryLog> findByDesignNoContainingIgnoreCaseOrCustomerNameContainingIgnoreCase(
            String designNo, String customerName, Pageable pageable);
	
	 @Query(
		      value = "SELECT * FROM enquiry_log e " +
		              "WHERE (:q IS NULL OR (LOWER(e.design_no) LIKE CONCAT('%', LOWER(:q), '%') OR LOWER(e.customer_name) LIKE CONCAT('%', LOWER(:q), '%'))) " +
		              "AND (:categoryIdStr IS NULL OR JSON_UNQUOTE(JSON_EXTRACT(e.product_snapshot, '$.categoryId')) = :categoryIdStr) " +
		              "AND (:subCategoryIdStr IS NULL OR JSON_UNQUOTE(JSON_EXTRACT(e.product_snapshot, '$.subCategoryId')) = :subCategoryIdStr) ",
		      countQuery = "SELECT count(*) FROM enquiry_log e " +
		              "WHERE (:q IS NULL OR (LOWER(e.design_no) LIKE CONCAT('%', LOWER(:q), '%') OR LOWER(e.customer_name) LIKE CONCAT('%', LOWER(:q), '%'))) " +
		              "AND (:categoryIdStr IS NULL OR JSON_UNQUOTE(JSON_EXTRACT(e.product_snapshot, '$.categoryId')) = :categoryIdStr) " +
		              "AND (:subCategoryIdStr IS NULL OR JSON_UNQUOTE(JSON_EXTRACT(e.product_snapshot, '$.subCategoryId')) = :subCategoryIdStr) ",
		      nativeQuery = true
		    )
		    Page<EnquiryLog> searchWithJsonFilters(
		            @Param("q") String q,
		            @Param("categoryIdStr") String categoryIdStr,
		            @Param("subCategoryIdStr") String subCategoryIdStr,
		            Pageable pageable);
		
}