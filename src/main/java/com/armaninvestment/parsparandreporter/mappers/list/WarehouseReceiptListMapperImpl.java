package com.armaninvestment.parsparandreporter.mappers.list;

import com.armaninvestment.parsparandreporter.dtos.list.WarehouseReceiptListDto;
import com.armaninvestment.parsparandreporter.entities.WarehouseReceipt;
import com.armaninvestment.parsparandreporter.entities.WarehouseReceiptItem;
import org.springframework.stereotype.Component;

@Component
public class WarehouseReceiptListMapperImpl implements WarehouseReceiptListMapper {
    public WarehouseReceiptListMapperImpl() {
    }

    public WarehouseReceiptListDto toDto(WarehouseReceipt warehouseReceipt) {
        if (warehouseReceipt == null) {
            return null;
        } else {
            WarehouseReceiptListDto warehouseReceiptListDto = new WarehouseReceiptListDto();
            warehouseReceiptListDto.setId(warehouseReceipt.getId());
            warehouseReceiptListDto.setWarehouseReceiptNumber(warehouseReceipt.getWarehouseReceiptNumber());
            warehouseReceiptListDto.setWarehouseReceiptDate(String.valueOf(warehouseReceipt.getWarehouseReceiptDate()));
            warehouseReceiptListDto.setWarehouseReceiptDescription(warehouseReceipt.getWarehouseReceiptDescription());
            warehouseReceiptListDto.setTotalQuantity(this.calculateTotalQuantity(warehouseReceipt));

            return warehouseReceiptListDto;
        }
    }

    private Long calculateTotalQuantity(WarehouseReceipt warehouseReceipt) {
        Long quantity = 0L;
        for (WarehouseReceiptItem item : warehouseReceipt.getWarehouseReceiptItems()) {
            quantity += item.getQuantity();
        }
        return quantity;
    }

    private Long calculateTotalAmount(WarehouseReceipt warehouseReceipt) {
        long amount = 0L;
        for (WarehouseReceiptItem item : warehouseReceipt.getWarehouseReceiptItems()) {
            amount += item.getQuantity() * item.getUnitPrice();
        }
        return amount;
    }
}
