package com.armaninvestment.parsparandreporter.mappers;

import com.armaninvestment.parsparandreporter.dtos.WarehouseReceiptDto;
import com.armaninvestment.parsparandreporter.entities.WarehouseReceipt;


public interface WarehouseReceiptMapper {
    WarehouseReceipt toEntity(WarehouseReceiptDto warehouseReceiptDto);

    default void linkWarehouseReceiptItems(WarehouseReceipt warehouseReceipt) {
        warehouseReceipt.getWarehouseReceiptItems().forEach(warehouseReceiptItem -> warehouseReceiptItem.setWarehouseReceipt(warehouseReceipt));
    }

    WarehouseReceiptDto toDto(WarehouseReceipt warehouseReceipt);

    WarehouseReceipt partialUpdate(WarehouseReceiptDto warehouseReceiptDto, WarehouseReceipt warehouseReceipt);
}