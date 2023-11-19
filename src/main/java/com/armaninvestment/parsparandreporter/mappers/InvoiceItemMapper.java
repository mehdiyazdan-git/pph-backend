package com.armaninvestment.parsparandreporter.mappers;

import com.armaninvestment.parsparandreporter.dtos.InvoiceItemDto;
import com.armaninvestment.parsparandreporter.entities.InvoiceItem;


public interface InvoiceItemMapper {
    InvoiceItem toEntity(InvoiceItemDto invoiceItemDto);

    InvoiceItemDto toDto(InvoiceItem invoiceItem);

    InvoiceItem partialUpdate(InvoiceItemDto invoiceItemDto, InvoiceItem invoiceItem);
}