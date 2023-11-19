package com.armaninvestment.parsparandreporter.mappers.dropdowns;

import com.armaninvestment.parsparandreporter.dtos.dropdowns.ReceiptDropDownDto;
import com.armaninvestment.parsparandreporter.entities.WarehouseReceipt;


public interface ReceiptDropDownMapper {
    ReceiptDropDownDto toDto(WarehouseReceipt warehouseReceipt);
}