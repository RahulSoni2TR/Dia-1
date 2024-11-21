package com.example.webapp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.webapp.models.Rate;

@Repository
public interface RateRepository extends JpaRepository<Rate, Long> {
	
	 Optional<Rate> findByCommodity(String commodity);
	
}
