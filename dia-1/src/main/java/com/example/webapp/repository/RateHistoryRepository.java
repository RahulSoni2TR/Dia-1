package com.example.webapp.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.webapp.models.RateHistory;

@Repository
public interface RateHistoryRepository extends JpaRepository<RateHistory, Long> {

    Optional<RateHistory> findTopByCommodityOrderByUpdatedAtDesc(String commodity);

    List<RateHistory> findByCommodityOrderByUpdatedAtDesc(String commodity);

    List<RateHistory> findByCommodityAndUpdatedAtBetweenOrderByUpdatedAtDesc(
            String commodity,
            LocalDateTime start,
            LocalDateTime end);
}