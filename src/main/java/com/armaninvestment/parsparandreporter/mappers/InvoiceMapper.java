package com.armaninvestment.parsparandreporter.mappers;

import com.armaninvestment.parsparandreporter.dtos.InvoiceDto;
import com.armaninvestment.parsparandreporter.entities.Invoice;

public interface InvoiceMapper {
    Invoice toEntity(InvoiceDto invoiceDto);

    InvoiceDto toDto(Invoice invoice);

    Invoice partialUpdate(InvoiceDto invoiceDto, Invoice invoice);

    default void linkInvoiceItems(Invoice invoice) {
        invoice.getInvoiceItems().forEach(invoiceItem -> invoiceItem.setInvoice(invoice));
    }
}