package com.armaninvestment.parsparandreporter.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO for {@link WarehouseReceipt}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WarehouseReceiptDtoByQuery implements Serializable {
    private Long id;
    private Long warehouseReceiptNumber;
    private LocalDate warehouseReceiptDate;
    private String warehouseReceiptDescription;
    private List<WarehouseReceiptItemDto> warehouseReceiptItems = new ArrayList<>();
    private Long customerId;
    private LocalDate reportDate;
    private Long invoiceNumber;
    private Long yearName;

    /**
     * DTO for {@link WarehouseReceiptItem}
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class WarehouseReceiptItemDto implements Serializable {
        private Long id;
        private Integer quantity;
        private Long unitPrice;
        private Long productId;
    }
}