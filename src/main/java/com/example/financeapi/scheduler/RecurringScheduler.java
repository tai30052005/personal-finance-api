package com.example.financeapi.scheduler;

import com.example.financeapi.service.RecurringTransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Tác vụ chạy NỀN theo lịch (scheduled job).
 *
 * @Scheduled: Spring tự gọi method theo biểu thức cron — KHÔNG cần ai bấm.
 * Đây là kỹ thuật "chạy tự động theo thời gian" mà CRUD thông thường không có.
 *
 * (Cần @EnableScheduling ở lớp khởi động để bật tính năng này.)
 */
@Component
public class RecurringScheduler {

    private static final Logger log = LoggerFactory.getLogger(RecurringScheduler.class);

    private final RecurringTransactionService recurringService;

    public RecurringScheduler(RecurringTransactionService recurringService) {
        this.recurringService = recurringService;
    }

    /**
     * Chạy mỗi ngày lúc 01:00 (giờ Việt Nam).
     * cron 6 trường: giây phút giờ ngày tháng thứ.
     */
    @Scheduled(cron = "0 0 1 * * *", zone = "Asia/Ho_Chi_Minh")
    public void generateDailyRecurring() {
        int created = recurringService.generateDueToday();
        if (created > 0) {
            log.info("Recurring scheduler: đã tự tạo {} giao dịch định kỳ.", created);
        }
    }
}
