package com.armaninvestment.parsparandreporter.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class NotInvoicedReceipt {
    private Long warehouseReceiptNumber;
    private String warehouseReceiptDescription;
    private String receiptDate;
    private Double totalValue;
    private Double totalQuantity;
}
