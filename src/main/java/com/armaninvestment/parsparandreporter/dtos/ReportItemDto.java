package com.armaninvestment.parsparandreporter.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO for {@link com.armaninvestment.parsparandreporter.entities.ReportItem}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReportItemDto implements Serializable {
    private Long id;
    private Long unitPrice;
    private Integer quantity;
    private Long customerId;
    private Long warehouseReceiptId;
}