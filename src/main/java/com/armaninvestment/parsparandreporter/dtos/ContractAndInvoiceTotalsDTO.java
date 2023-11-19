package com.armaninvestment.parsparandreporter.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ContractAndInvoiceTotalsDTO {
    private Long cumulativeContractAmount;
    private Long cumulativeContractQuantity;
    private Long contractAdvancedPayment;
    private Long contractPerformanceBond;
    private Long contractInsuranceDeposit;
    private Long totalCommittedContractAmount;
    private Long totalRemainingContractAmount;
    private Long totalCommittedContractCount;
    private Long totalInvoiceCount;
    private Long totalConsumedContractAdvancedPayment;
    private Long totalOutstandingContractAdvancedPayment;
    private Long totalCommitedPerformanceBond;
    private Long totalRemainingPerformanceBond;
    private Long totalCommitedInsuranceDeposit;
    private Long totalRemainingInsuranceDeposit;
}

