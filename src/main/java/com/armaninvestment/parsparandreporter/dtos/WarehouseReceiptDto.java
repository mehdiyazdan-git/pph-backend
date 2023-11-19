package com.armaninvestment.parsparandreporter.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO for {@link com.armaninvestment.parsparandreporter.entities.WarehouseReceipt}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WarehouseReceiptDto implements Serializable {
    private Long id;
    private Long warehouseReceiptNumber;
    private LocalDate warehouseReceiptDate;
    private String warehouseReceiptDescription;
    private Long invoiceNumber;
    private LocalDate reportDate;
    private List<WarehouseReceiptItemDto> warehouseReceiptItems = new ArrayList<>();
    private Long customerId;
    private Long yearName;
}