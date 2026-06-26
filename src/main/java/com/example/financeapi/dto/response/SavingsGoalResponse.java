package com.example.financeapi.dto.response;

import com.example.financeapi.entity.SavingsGoal;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

public record SavingsGoalResponse(
        Long id,
        String name,
        BigDecimal targetAmount,
        BigDecimal currentAmount,
        LocalDate deadline,
        double progressPercent,
        boolean completed
) {
    public static SavingsGoalResponse from(SavingsGoal g) {
        BigDecimal target = g.getTargetAmount();
        BigDecimal current = g.getCurrentAmount();
        double pct = target.compareTo(BigDecimal.ZERO) > 0
                ? current.divide(target, 4, RoundingMode.HALF_UP).doubleValue() * 100
                : 0;
        boolean done = current.compareTo(target) >= 0;
        return new SavingsGoalResponse(
                g.getId(), g.getName(), target, current, g.getDeadline(), pct, done);
    }
}
