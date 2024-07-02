package com.armaninvestment.parsparandreporter.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class InvoicesByContractIdDto {
    private Long invoiceId;
    private Long invoiceNumber;
    private String invoiceDate;
    private Long invoiceQuantity;
    private Long invoiceAmount;
    private Long invoiceAddedValueTax;
    private Long advancedPayment;
    private Long performanceBound;
    private Long insuranceDeposit;
}
