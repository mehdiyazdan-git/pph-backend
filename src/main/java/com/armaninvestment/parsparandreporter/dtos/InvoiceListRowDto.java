package com.armaninvestment.parsparandreporter.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceListRowDto {
    private Long invoiceId;
    private Long invoiceNumber;
    private String issuedDate;
    private String dueDate;
    private String customerCode;
    private String customerName;
    private String salesType;
    private BigDecimal totalAmount;
    private Long totalQuantity;
    private Long itemCount;
}
