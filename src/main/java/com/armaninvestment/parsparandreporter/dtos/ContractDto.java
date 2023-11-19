package com.armaninvestment.parsparandreporter.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * DTO for {@link com.armaninvestment.parsparandreporter.entities.Contract}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContractDto implements Serializable {
    private Long id;
    private String contractNumber;
    private String contractDescription;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long advancePayment;
    private Long performanceBond;
    private Long insuranceDeposit;
    private Long cumulativeInvoiceAmount;
    private Long cumulativeInvoiceQuantity;
    private Long cumulativeContractAmount;
    private Long cumulativeContractQuantity;
    private Long remainingContractObligations;
    private Long remainingContractQuantityObligations;
    private Long cumulativeContractPerformanceBonds;
    private Long cumulativeContractInsuranceDeposits;
    private Long consumedAdvancePayment;
    private Long outstandingAdvancePayment;
    private Long customerId;
    private Set<ContractItemDto> contractItems = new LinkedHashSet<>();
    private Long yearName;
}