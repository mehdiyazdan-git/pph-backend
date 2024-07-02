package com.armaninvestment.parsparandreporter.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * DTO for {@link Contract}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContractDtoByQuery implements Serializable {
    private Long contractId;
    private String contractNumber;
    private String contractDescription;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double advancePaymentCoefficient;
    private Double contractPerformanceBondCoefficient;
    private Double contractInsuranceDepositCoefficient;
    private Long customerId;
    private Long yearId;
    private Long cumulativeContractAmount;
    private Long cumulativeContractQuantity;
    private Long contractAdvancedPaymentAmount;
    private Long contractPerformanceBoundAmount;
    private Long contractInsuranceDepositAmount;
    private Long totalCommittedContractAmount;
    private Long totalCommittedContractCount;
    private Long totalRemainingContractAmount;
    private Long totalRemainingContractCount;
    private Long totalConsumedContractAdvancedPayment;
    private Long totalOutstandingContractAdvancedPayment;
    private Long totalCommittedPerformanceBound;
    private Long totalRemainingPerformanceBond;
    private Long totalCommittedInsuranceDeposit;
    private Long totalRemainingInsuranceDeposit;
    private Set<ContractItemDto> contractItems = new LinkedHashSet<>();

    /**
     * DTO for {@link ContractItem}
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ContractItemDto implements Serializable {
        private Long id;
        private Long unitPrice;
        private Long quantity;
        private Long productId;
    }
}