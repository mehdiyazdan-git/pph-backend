package com.armaninvestment.parsparandreporter.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO for {@link com.armaninvestment.parsparandreporter.entities.WarehouseReceiptItem}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WarehouseReceiptItemDto implements Serializable {
    private Long id;
    private Integer quantity;
    private Long unitPrice;
    private Long productId;
}