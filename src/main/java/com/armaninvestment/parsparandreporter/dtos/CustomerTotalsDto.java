package com.armaninvestment.parsparandreporter.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CustomerTotalsDto {
    private Long customerId;
    private String customerName;
    private Long totalAmount;
    private Long totalQuantity;
    private Long totalPayment;
    private Long totalInvoiceCount;
}
