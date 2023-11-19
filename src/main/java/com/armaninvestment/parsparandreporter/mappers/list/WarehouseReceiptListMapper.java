package com.armaninvestment.parsparandreporter.mappers.list;

import com.armaninvestment.parsparandreporter.dtos.list.WarehouseReceiptListDto;
import com.armaninvestment.parsparandreporter.entities.WarehouseReceipt;


public interface WarehouseReceiptListMapper {
    WarehouseReceiptListDto toDto(WarehouseReceipt warehouseReceipt);
}