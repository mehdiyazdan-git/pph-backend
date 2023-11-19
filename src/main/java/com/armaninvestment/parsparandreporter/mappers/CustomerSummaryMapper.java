package com.armaninvestment.parsparandreporter.mappers;

import com.armaninvestment.parsparandreporter.dtos.CustomerSummaryDto;
import com.armaninvestment.parsparandreporter.dtos.WarehouseReceiptDto;
import com.armaninvestment.parsparandreporter.entities.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public interface CustomerSummaryMapper {

    CustomerSummaryDto toDto(Customer customer);

    default List<WarehouseReceiptDto> extractInvoiceReceipts(Invoice invoice) {
        List<WarehouseReceiptDto> warehouseReceipts = new ArrayList<>();
        Set<InvoiceItem> invoiceItems = invoice.getInvoiceItems();

        for (InvoiceItem invoiceItem : invoiceItems) {
            WarehouseReceipt warehouseReceipt = invoiceItem.getWarehouseReceipt();
            if (warehouseReceipt != null) {
                WarehouseReceiptDto warehouseReceiptDto = new WarehouseReceiptDto();
                warehouseReceiptDto.setId(warehouseReceipt.getId());
                warehouseReceiptDto.setWarehouseReceiptNumber(warehouseReceipt.getWarehouseReceiptNumber());
                warehouseReceipts.add(warehouseReceiptDto);
            }
        }

        return warehouseReceipts;
    }

    default List<WarehouseReceiptDto> extractReportReceipts(Customer customer) {
        List<ReportItem> reportItems = customer.getReportItems();
        List<WarehouseReceiptDto> warehouseReceipts = new ArrayList<>();


        for (ReportItem reportItem : reportItems) {
            WarehouseReceipt warehouseReceipt = reportItem.getWarehouseReceipt();
            if (warehouseReceipt != null) {
                WarehouseReceiptDto warehouseReceiptDto = new WarehouseReceiptDto();
                warehouseReceiptDto.setId(warehouseReceipt.getId());
                warehouseReceiptDto.setWarehouseReceiptNumber(warehouseReceipt.getWarehouseReceiptNumber());
                warehouseReceipts.add(warehouseReceiptDto);
            }
        }

        return warehouseReceipts;
    }
}
