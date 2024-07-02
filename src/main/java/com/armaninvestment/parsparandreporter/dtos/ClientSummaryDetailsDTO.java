package com.armaninvestment.parsparandreporter.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ClientSummaryDetailsDTO {

    private String contractNumber;
    private Long id;
    private Long invoiceNumber;
    private String issuedDate;
    private Long totalQuantity;
    private Long totalAmount;
    private Long advancedPayment;
    private Long insuranceDeposit;
    private Long performanceBound;
}

