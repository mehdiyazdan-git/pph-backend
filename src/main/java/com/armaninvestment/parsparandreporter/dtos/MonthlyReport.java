package com.armaninvestment.parsparandreporter.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class MonthlyReport {
    private Long id;
    private String name;
    private BigDecimal totalAmount;
    private Long totalCount;
    private BigDecimal avgPrice;
}
