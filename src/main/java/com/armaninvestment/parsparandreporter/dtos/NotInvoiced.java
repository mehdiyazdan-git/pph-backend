package com.armaninvestment.parsparandreporter.dtos;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotInvoiced {
    private Long id;
    private Long receiptNo;
    private String description;
    private String receiptDate;
    private String customerCode;
    private String customerName;
    private BigDecimal totalAmount;
    private Long totalQuantity;
    private Long itemCount;
}

