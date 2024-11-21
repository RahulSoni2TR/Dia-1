package com.example.webapp.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.webapp.models.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
    // Additional query methods can be defined here
	List<Product> findByCategoryId(Long categoryId);
	Page<Product> findByCategoryId(Integer category, Pageable pageable);
}
