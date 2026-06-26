package com.example.financeapi.repository;

import com.example.financeapi.entity.SavingsGoal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SavingsGoalRepository extends JpaRepository<SavingsGoal, Long> {

    List<SavingsGoal> findByUserIdOrderByIdAsc(Long userId);

    Optional<SavingsGoal> findByIdAndUserId(Long id, Long userId);
}
