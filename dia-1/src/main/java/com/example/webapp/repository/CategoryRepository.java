package com.example.webapp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.webapp.models.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {

	Optional<Category> findById(Long id);

}