package com.example.webapp.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.example.webapp.models.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
    // Additional query methods can be defined here
	List<Product> findByCategoryIdAndOrdersIsNotNullAndDesignNoIsNotNull(Long categoryId);
//	Page<Product> findByCategoryId(Integer category, Pageable pageable);
	Optional<Product> findByDesignNo(String designNo);
	void deleteByDesignNo(String productId);
	  boolean existsByDesignNo(String designNo);
	
	@Query("SELECT p FROM Product p " +
		       "WHERE p.categoryId  = :categoryId AND p.createDateTime BETWEEN :startDate AND :endDate")
	Page<Product> findProductsByCategoryAndDateRange(
		        @Param("categoryId") Long categoryId,
		        @Param("startDate") LocalDateTime startDate,
		        @Param("endDate") LocalDateTime endDate,
		        Pageable pageable);
	// Search products by name (with a LIKE query) and category id
    Page<Product> findByItemContainingAndCategoryId(String searchTerm, Integer categoryId, Pageable pageable);

    // Optionally, you can also add custom queries for better matching using a full-text search or fuzzy matching.
    @Query("SELECT p FROM Product p WHERE p.item LIKE %:searchTerm% AND p.categoryId = :categoryId")
    Page<Product> searchProductsByNameAndCategory(String searchTerm, Integer categoryId, Pageable pageable);
    
    Page<Product> findByCategoryIdInAndItemContainingIgnoreCase(
            Collection<Integer> categories,
            String searchTerm,
            Pageable pageable
    );

    Page<Product> findBysubCategoryIdInAndItemContainingIgnoreCase(
            Collection<Long> categories,
            String searchTerm,
            Pageable pageable
    );

//    Page<Product> findProductsByCategoryAndSubCategoryAndDateRange(Long categoryId, Long subCategoryId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    List<Product> findByCategoryIdAndSubCategoryIdAndOrdersIsNotNullAndDesignNoIsNotNull(Long categoryId, Long subCategoryId);



    @Query("SELECT p FROM Product p " +
    	       "WHERE (:searchTerm IS NULL OR :searchTerm = '' OR p.item LIKE :searchTerm)")
    	Page<Product> findProductsWithoutCategoryAndName(@Param("searchTerm") String searchTerm, 
    	                                                 Pageable pageable);

 // Without category
    Page<Product> findByItemContainingIgnoreCaseAndDesignNoIsNotNullAndDesignNoNot(
            String item, String empty, Pageable pageable);

    Page<Product> findByDesignNoContainingIgnoreCaseAndDesignNoIsNotNullAndDesignNoNot(
            String designNo, String empty, Pageable pageable);

    Page<Product> findByOrders_OrderIdContainingIgnoreCaseAndDesignNoIsNotNullAndDesignNoNot(
            String orderId, String empty, Pageable pageable);

    // With category
    Page<Product> findByItemContainingIgnoreCaseAndCategoryIdInAndDesignNoIsNotNullAndDesignNoNot(
            String item, List<Integer> categoryIds, String empty, Pageable pageable);

    Page<Product> findByDesignNoContainingIgnoreCaseAndCategoryIdInAndDesignNoIsNotNullAndDesignNoNot(
            String designNo, List<Integer> categoryIds, String empty, Pageable pageable);

    Page<Product> findByOrders_OrderIdContainingIgnoreCaseAndCategoryIdInAndDesignNoIsNotNullAndDesignNoNot(
            String orderId, List<Integer> categoryIds, String empty, Pageable pageable);

    // Subcategory
    Page<Product> findByItemContainingIgnoreCaseAndSubCategoryIdInAndDesignNoIsNotNullAndDesignNoNot(
            String item, List<Long> subCategoryIds, String empty, Pageable pageable);

    Page<Product> findByDesignNoContainingIgnoreCaseAndSubCategoryIdInAndDesignNoIsNotNullAndDesignNoNot(
            String designNo, List<Long> subCategoryIds, String empty, Pageable pageable);

    Page<Product> findByOrders_OrderIdContainingIgnoreCaseAndSubCategoryIdInAndDesignNoIsNotNullAndDesignNoNot(
            String orderId, List<Long> subCategoryIds, String empty, Pageable pageable);

    
    @Modifying
    @Transactional
    @Query("UPDATE Product p SET p.orders = null, p.designNo = null WHERE p.designNo = :designNo")
    int resetProductMappingByDesignNo(@Param("designNo") String designNo);
    
    Page<Product> findByCategoryIdAndCreateDateTimeBetweenAndOrdersIsNotNullAndDesignNoIsNotNull(
            Long categoryId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable);

        Page<Product> findByCategoryIdAndSubCategoryIdAndCreateDateTimeBetweenAndOrdersIsNotNullAndDesignNoIsNotNull(
            Long categoryId,
            Long subCategoryId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable);
        
        List<Product> findByDesignNoIn(List<String> designNos);
        
        Page<Product>
        findByItemContainingIgnoreCaseOrDesignNoContainingIgnoreCaseAndDesignNoIsNotNullAndDesignNoNot(
            String item,
            String designNo,
            String empty,
            Pageable pageable
        );
        
        Page<Product>
        findByItemContainingIgnoreCaseOrDesignNoContainingIgnoreCaseAndCategoryIdInAndDesignNoIsNotNullAndDesignNoNot(
            String item,
            String designNo,
            List<Integer> categoryIds,
            String empty,
            Pageable pageable
        );

        
        Page<Product>
        findByItemContainingIgnoreCaseOrDesignNoContainingIgnoreCaseAndSubCategoryIdInAndDesignNoIsNotNullAndDesignNoNot(
            String item,
            String designNo,
            List<Long> subCategoryIds,
            String empty,
            Pageable pageable
        );

        /* =====================================================
        DESIGN NO – EXACT MATCH
        ===================================================== */
     Page<Product> findByDesignNo(
             Long designNo,
             Pageable pageable
     );


     /* =====================================================
        ALL – Product Name (LIKE) OR Design No (EXACT)
        ===================================================== */
     @Query("""
         SELECT p FROM Product p
         WHERE
           (
             LOWER(p.item) LIKE LOWER(CONCAT('%', :term, '%'))
             OR p.designNo = :designNo
           )
           AND p.designNo IS NOT NULL
           AND p.designNo <> ''
     """)
     Page<Product> searchByNameOrDesign(
             @Param("term") String term,
             @Param("designNo") Long designNo,
             Pageable pageable
     );

     Page<Product> findBySubCategoryIdAndItemContainingIgnoreCaseAndDesignNoIsNotNullAndDesignNoNot(
    		    long subCategoryId, String term, String notValue, Pageable pageable
    		);

    		Page<Product> findBySubCategoryIdAndDesignNoContainingIgnoreCaseAndDesignNoIsNotNullAndDesignNoNot(
    		    long subCategoryId, String term, String notValue, Pageable pageable
    		);
     
}
