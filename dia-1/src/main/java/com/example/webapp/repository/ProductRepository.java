package com.example.webapp.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.webapp.models.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
    // Additional query methods can be defined here
	List<Product> findByCategoryId(Long categoryId);
	Page<Product> findByCategoryId(Integer category, Pageable pageable);
	Optional<Product> findByDesignNo(String productId);
	void deleteByDesignNo(String productId);
	  boolean existsByDesignNo(String designNo);
	
	@Query("SELECT p FROM Product p " +
		       "WHERE p.categoryId  = :categoryId AND p.createDateTime BETWEEN :startDate AND :endDate")
		List<Product> findProductsByCategoryAndDateRange(
		        @Param("categoryId") Long categoryId,
		        @Param("startDate") LocalDateTime startDate,
		        @Param("endDate") LocalDateTime endDate,
		        Pageable pageable);
	// Search products by name (with a LIKE query) and category id
    Page<Product> findByItemContainingAndCategoryId(String searchTerm, Integer categoryId, Pageable pageable);

    // Optionally, you can also add custom queries for better matching using a full-text search or fuzzy matching.
    @Query("SELECT p FROM Product p WHERE p.item LIKE %:searchTerm% AND p.categoryId = :categoryId")
    Page<Product> searchProductsByNameAndCategory(String searchTerm, Integer categoryId, Pageable pageable);
    
    @Query("SELECT p FROM Product p " +
    	       "WHERE p.item LIKE CONCAT('%', :searchTerm, '%') " +
    	       "AND (:categoryId IS NULL OR p.categoryId = :categoryId)")
    	Page<Product> findProductsByCategoryAndName(@Param("searchTerm") String productName,
    	                                             @Param("categoryId") Integer categoryId, 
    	                                             Pageable pageable);
    @Query("SELECT p FROM Product p " +
    	       "WHERE (:searchTerm IS NULL OR :searchTerm = '' OR p.item LIKE :searchTerm)")
    	Page<Product> findProductsWithoutCategoryAndName(@Param("searchTerm") String searchTerm, 
    	                                                 Pageable pageable);



}
