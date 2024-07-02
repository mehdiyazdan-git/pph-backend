package com.armaninvestment.parsparandreporter.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class MonthlyCustomerInvoiceSummaryDTO implements Serializable {
    private Integer monthNumber;
    private String persianCaption;
    private Long paymentAmount;
    private Long invoiceCount;
    private Long totalAmount;
    private Long totalQuantity;
}
