package com.example.financeapi.service;

import com.example.financeapi.dto.request.SavingsGoalRequest;
import com.example.financeapi.dto.response.SavingsGoalResponse;
import com.example.financeapi.entity.SavingsGoal;
import com.example.financeapi.entity.User;
import com.example.financeapi.exception.ResourceNotFoundException;
import com.example.financeapi.repository.SavingsGoalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class SavingsGoalService {

    private final SavingsGoalRepository goalRepository;
    private final CurrentUserService currentUserService;

    public SavingsGoalService(SavingsGoalRepository goalRepository,
                              CurrentUserService currentUserService) {
        this.goalRepository = goalRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional(readOnly = true)
    public List<SavingsGoalResponse> listMine() {
        User user = currentUserService.getCurrentUser();
        return goalRepository.findByUserIdOrderByIdAsc(user.getId())
                .stream().map(SavingsGoalResponse::from).toList();
    }

    @Transactional
    public SavingsGoalResponse create(SavingsGoalRequest request) {
        User user = currentUserService.getCurrentUser();
        SavingsGoal goal = new SavingsGoal(user, request.name(), request.targetAmount(), request.deadline());
        goalRepository.save(goal);
        return SavingsGoalResponse.from(goal);
    }

    @Transactional
    public SavingsGoalResponse update(Long id, SavingsGoalRequest request) {
        SavingsGoal goal = getOwnedOrThrow(id);
        goal.setName(request.name());
        goal.setTargetAmount(request.targetAmount());
        goal.setDeadline(request.deadline());
        return SavingsGoalResponse.from(goal);
    }

    @Transactional
    public void delete(Long id) {
        goalRepository.delete(getOwnedOrThrow(id));
    }

    /** Góp thêm tiền vào mục tiêu (cộng dồn vào currentAmount). */
    @Transactional
    public SavingsGoalResponse contribute(Long id, BigDecimal amount) {
        SavingsGoal goal = getOwnedOrThrow(id);
        goal.setCurrentAmount(goal.getCurrentAmount().add(amount));
        return SavingsGoalResponse.from(goal);
    }

    private SavingsGoal getOwnedOrThrow(Long id) {
        User user = currentUserService.getCurrentUser();
        return goalRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Mục tiêu tiết kiệm", id));
    }
}
