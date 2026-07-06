package com.example.financeapi.dto.response;

import java.time.LocalDate;

/** Một ngày có ghi chép: ngày + số giao dịch đã ghi (cho heatmap "chăm vườn"). */
public record ActivityDay(
        LocalDate date,
        long count
) {
}
