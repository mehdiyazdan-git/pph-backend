package com.armaninvestment.parsparandreporter.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO for {@link com.armaninvestment.parsparandreporter.entities.Contract}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContractSummaryDto implements Serializable {
    private Long id;
    private String contractNumber;
    private String contractDescription;
    private Long cumulativeInvoiceAmount;
    private Long cumulativeContractAmount;
    private Long cumulativeContractQuantity;
    private Long remainingContractObligations;
    private Long consumedAdvancePayment;
    private Long outstandingAdvancePayment;
}
