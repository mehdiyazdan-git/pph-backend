package com.armaninvestment.parsparandreporter.mappers.dropdowns;

import com.armaninvestment.parsparandreporter.dtos.dropdowns.ReceiptDropDownDto;
import com.armaninvestment.parsparandreporter.entities.Customer;
import com.armaninvestment.parsparandreporter.entities.WarehouseReceipt;
import org.springframework.stereotype.Component;

@Component
public class ReceiptDropDownMapperImpl implements ReceiptDropDownMapper {
    public ReceiptDropDownMapperImpl() {
    }

    public ReceiptDropDownDto toDto(WarehouseReceipt warehouseReceipt) {
        if (warehouseReceipt == null) {
            return null;
        } else {
            ReceiptDropDownDto receiptDropDownDto = new ReceiptDropDownDto();
            receiptDropDownDto.setId(warehouseReceipt.getId());
            return receiptDropDownDto;
        }
    }

    private String customerName(WarehouseReceipt warehouseReceipt) {
        if (warehouseReceipt == null) {
            return null;
        } else {
            Customer customer = warehouseReceipt.getCustomer();
            if (customer == null) {
                return null;
            } else {
                return customer.getName();
            }
        }
    }
}