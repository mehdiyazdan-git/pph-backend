package com.armaninvestment.parsparandreporter.dtos.list;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * DTO for {@link com.armaninvestment.parsparandreporter.entities.WarehouseReceipt}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WarehouseReceiptListDto implements Serializable {
    private Long id;
    private Long warehouseReceiptNumber;
    private LocalDate warehouseReceiptDate;
    private String warehouseReceiptDescription;
    private Long totalQuantity;
    private Long totalAmount;
}