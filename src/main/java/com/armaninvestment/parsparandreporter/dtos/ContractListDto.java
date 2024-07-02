package com.armaninvestment.parsparandreporter.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContractListDto implements Serializable {
    private Long id;
    private String contractNumber;
    private String contractDescription;
    private LocalDate startDate;
    private LocalDate endDate;
    private String customerName;
    private BigDecimal totalAmount;
    private BigDecimal totalQuantity;
}