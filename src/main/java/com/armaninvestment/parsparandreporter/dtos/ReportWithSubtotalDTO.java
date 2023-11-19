package com.armaninvestment.parsparandreporter.dtos;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ReportWithSubtotalDTO {
    private Long id;
    private LocalDate date;
    private Long totalCount;
    private BigDecimal totalAmount;
}
