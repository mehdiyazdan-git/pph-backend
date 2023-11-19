package com.armaninvestment.parsparandreporter.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CustomerReport {
    private String reportDate;
    private String reportExplanation;
    private BigDecimal totalAmount;
    private Long totalQuantity;
}
