package com.example.webapp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.webapp.models.Orders;
import com.example.webapp.models.Product;


public interface OrderRepository extends JpaRepository<Orders, Long>{

	
	// Get unassigned orders for a category
	List<Orders> findByCategoryIdAndIsAssignedFalse(Long categoryId);

	// Check if custom order ID already exists
	Optional<Orders> findByOrderId(String orderId);
	
	Optional<Orders> findByAssignedProduct(Product assignedProductId);

    boolean existsByOrderId(String orderId);

    @Modifying
    @Query("UPDATE Orders o SET o.isAssigned = false, o.assignedProduct = null WHERE o.orderId = :orderId")
    void resetOrderMapping(@Param("orderId") String orderId);
    
 // Fetch all orderIds for a given category
    @Query("select o.orderId from Orders o where o.categoryId = :categoryId")
    List<String> findOrderIdsByCategory(@Param("categoryId") Integer categoryId);

 // Remove nativeQuery=true - use JPQL instead

    @Modifying
    @Query(value = "DELETE FROM orders WHERE is_assigned = 0 AND assigned_product_id IS NULL " +
                   "AND (order_id LIKE 'DIA-%' OR order_id LIKE 'OS-%' OR order_id LIKE 'PG-%' OR " +
                   "     order_id LIKE 'VIL-%' OR order_id LIKE 'JAD-%')", 
           nativeQuery = true)
    int deleteOldUnassignedOrders();

    @Query("SELECT o FROM Orders o WHERE o.isAssigned = false AND o.categoryId IN :categoryIds")
    List<Orders> findByCategoryIdsAndIsAssignedFalse(@Param("categoryIds") List<Integer> categoryIds);

    @Query("SELECT o FROM Orders o WHERE o.isAssigned = false")
    List<Orders> findAllByIsAssignedFalse();

    
}
