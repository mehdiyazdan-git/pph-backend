package com.armaninvestment.parsparandreporter.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * DTO for {@link NotInvoiced}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotInvoicedDto implements Serializable {
    private Long id;
    private Long receiptNo;
    private String description;
    private String customerCode;
    private String customerName;
    private BigDecimal totalAmount;
    private Long totalQuantity;
    private Long itemCount;
}