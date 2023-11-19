package com.armaninvestment.parsparandreporter.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SalesByYearGroupByMonth {
    private Short monthNumber;
    private String monthName;
    private BigDecimal totalAmount;
    private Long totalQuantity;
}
