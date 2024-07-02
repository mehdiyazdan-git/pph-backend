package com.armaninvestment.parsparandreporter.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class InvoiceUploadDto implements Serializable {
    private Long invoiceId;
    private Long invoiceNumber;
    private String issuedDate;
    private String dueDate;
    private String salesType;
    private String contractNumber;
    private Integer statusId;
    private Long year;
    private String customerCode;
    private Long advancedPayment;
    private Long performanceBound;
    private Long insuranceDeposit;
    private String productType;
    private Integer quantity;
    private Long unitPrice;
    private Long warehouseReceiptNumber;
    private String warehouseReceiptDate;
}
