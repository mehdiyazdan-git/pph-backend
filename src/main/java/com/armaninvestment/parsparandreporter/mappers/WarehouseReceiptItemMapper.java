package com.armaninvestment.parsparandreporter.mappers;

import com.armaninvestment.parsparandreporter.dtos.WarehouseReceiptItemDto;
import com.armaninvestment.parsparandreporter.entities.WarehouseReceiptItem;


public interface WarehouseReceiptItemMapper {

    WarehouseReceiptItem toEntity(WarehouseReceiptItemDto warehouseReceiptItemDto);

    WarehouseReceiptItemDto toDto(WarehouseReceiptItem warehouseReceiptItem);

    WarehouseReceiptItem partialUpdate(WarehouseReceiptItemDto warehouseReceiptItemDto, WarehouseReceiptItem warehouseReceiptItem);
}