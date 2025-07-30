package com.example.webapp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.webapp.models.Orders;
import com.example.webapp.models.Product;


public interface OrderRepository extends JpaRepository<Orders, Long>{

	
	// Get unassigned orders for a category
	List<Orders> findByCategoryIdAndIsAssignedFalse(Long categoryId);

	// Check if custom order ID already exists
	Optional<Orders> findByOrderId(String orderId);
	
	Optional<Orders> findByAssignedProduct(Product assignedProductId);

    boolean existsByOrderId(String orderId);

}
