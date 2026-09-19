package com.restaurant.pos.repository;

import com.restaurant.pos.entity.DailyClosing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface DailyClosingRepository extends JpaRepository<DailyClosing, Long> {

    Optional<DailyClosing> findByClosingDate(LocalDate closingDate);

    boolean existsByClosingDate(LocalDate closingDate);
}