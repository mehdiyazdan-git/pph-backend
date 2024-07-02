package com.armaninvestment.parsparandreporter.dtos.list;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * DTO for {@link com.armaninvestment.parsparandreporter.entities.WarehouseReceipt}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WarehouseReceiptListDto implements Serializable {
    private Long id;
    private Long warehouseReceiptNumber;
    private String warehouseReceiptDate;
    private String warehouseReceiptDescription;
    private String customerName;
    private String reportDate;
    private Long invoiceNumber;
    private Long totalQuantity;
    private BigDecimal totalAmount;
}