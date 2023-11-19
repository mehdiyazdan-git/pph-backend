package com.armaninvestment.parsparandreporter.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO for {@link com.armaninvestment.parsparandreporter.entities.InvoiceItem}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceItemDto1 implements Serializable {
    private Long id;
    private Long productId;
    private Integer quantity;
    private Long unitPrice;
    private Long warehouseReceiptId;
}