package com.armaninvestment.parsparandreporter.mappers;

import com.armaninvestment.parsparandreporter.dtos.CustomerSummaryDto;
import com.armaninvestment.parsparandreporter.dtos.WarehouseReceiptDto;
import com.armaninvestment.parsparandreporter.entities.*;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CustomerSummaryMapperImpl implements CustomerSummaryMapper {

    @Override
    public CustomerSummaryDto toDto(Customer customer) {
        CustomerSummaryDto customerSummaryDto = new CustomerSummaryDto();
        customerSummaryDto.setId(customer.getId());
        customerSummaryDto.setCustomerName(customer.getName());
        customerSummaryDto.setIssuedInvoiceAmount(this.calculateIssuedInvoiceAmounts(customer));
        customerSummaryDto.setIssuedInvoicesCount(this.calculateIssuedInvoiceCount(customer));
        customerSummaryDto.setNotIssuedInvoiceAmount(this.calculateNotIssuedInvoiceAmounts(customer));
        customerSummaryDto.setNotIssuedInvoicesCount(this.calculateNotIssuedInvoiceCount(customer));
        return customerSummaryDto;
    }

    protected Long calculateIssuedInvoiceAmounts(Customer customer) {
        Set<Invoice> invoiceList = extractInvoiceSet(customer);
        long issuedInvoiceAmounts = 0L;
        for (Invoice invoice : invoiceList) {
            for (InvoiceItem invoiceItem : invoice.getInvoiceItems()) {
                issuedInvoiceAmounts += invoiceItem.getUnitPrice() * invoiceItem.getQuantity();
            }
        }
        return issuedInvoiceAmounts;
    }

    protected Long calculateIssuedInvoiceCount(Customer customer) {
        Set<Invoice> invoiceList = extractInvoiceSet(customer);
        long issuedInvoiceCount = 0L;
        for (Invoice invoice : invoiceList) {
            for (InvoiceItem invoiceItem : invoice.getInvoiceItems()) {
                issuedInvoiceCount += invoiceItem.getQuantity();
            }
        }
        return issuedInvoiceCount;
    }

    protected Long calculateNotIssuedInvoiceAmounts(Customer customer) {

        Set<Invoice> invoiceList = extractInvoiceSet(customer);
        Map<Long, InvoiceItem> invoiceItemMap = extractInvoiceItemSet(invoiceList);

        List<WarehouseReceiptDto> invoiceReceiptList = new ArrayList<>();
        List<WarehouseReceiptDto> reportReceiptList = extractReportReceipts(customer);

        for (Invoice invoice : invoiceList) {
            List<WarehouseReceiptDto> warehouseReceiptDtos = extractInvoiceReceipts(invoice);
            invoiceReceiptList.addAll(warehouseReceiptDtos);
        }
        long nonIssuedInvoiceAmount = 0L;

        for (WarehouseReceiptDto invoiceReceipt : invoiceReceiptList) {
            if (!reportReceiptList.contains(invoiceReceipt)) {
                nonIssuedInvoiceAmount += invoiceItemMap.get(invoiceReceipt.getId()).getQuantity() * invoiceItemMap.get(invoiceReceipt.getId()).getUnitPrice();
            }
        }

        return nonIssuedInvoiceAmount;
    }

    protected Long calculateNotIssuedInvoiceCount(Customer customer) {

        Set<Invoice> invoiceList = extractInvoiceSet(customer);
        Map<Long, InvoiceItem> invoiceItemMap = extractInvoiceItemSet(invoiceList);

        List<WarehouseReceiptDto> invoiceReceiptList = new ArrayList<>();
        List<WarehouseReceiptDto> reportReceiptList = extractReportReceipts(customer);

        for (Invoice invoice : invoiceList) {
            List<WarehouseReceiptDto> warehouseReceiptDtos = extractInvoiceReceipts(invoice);
            invoiceReceiptList.addAll(warehouseReceiptDtos);
        }
        long nonIssuedInvoiceAmount = 0L;

        for (WarehouseReceiptDto invoiceReceipt : invoiceReceiptList) {
            if (!reportReceiptList.contains(invoiceReceipt)) {
                nonIssuedInvoiceAmount += invoiceItemMap.get(invoiceReceipt.getId()).getQuantity();
            }
        }

        return nonIssuedInvoiceAmount;
    }

    protected Set<Invoice> extractInvoiceSet(Customer customer) {
        Set<Contract> contracts = customer.getContracts();
        Set<Invoice> invoiceList = new HashSet<>();
        for (Contract contract : contracts) {
            invoiceList.addAll(contract.getInvoices());
        }
        return invoiceList;
    }

    protected Map<Long, InvoiceItem> extractInvoiceItemSet(Set<Invoice> invoiceSet) {

        Map<Long, InvoiceItem> invoiceItemMap = new HashMap<>();

        for (Invoice invoice : invoiceSet) {
            for (InvoiceItem invoiceItem : invoice.getInvoiceItems()) {
                WarehouseReceipt warehouseReceipt = invoiceItem.getWarehouseReceipt();
                if (warehouseReceipt == null) continue;
                invoiceItemMap.put(warehouseReceipt.getId(), invoiceItem);
            }
        }

        return invoiceItemMap;

    }
}
