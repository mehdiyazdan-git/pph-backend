package com.armaninvestment.parsparandreporter.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CustomerInvoiceDto {
    private Long id;
    private Long invoiceNumber;
    private String invoiceDate;
    private BigDecimal totalAmount;
    private Long totalQuantity;
}
