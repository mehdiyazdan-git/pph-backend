package com.armaninvestment.parsparandreporter.mappers;

import com.armaninvestment.parsparandreporter.dtos.InvoiceStatusDto;
import com.armaninvestment.parsparandreporter.entities.InvoiceStatus;

public interface InvoiceStatusMapper {
    InvoiceStatus toEntity(InvoiceStatusDto invoiceStatusDto);

    InvoiceStatusDto toDto(InvoiceStatus invoiceStatus);

    InvoiceStatus partialUpdate(InvoiceStatusDto invoiceStatusDto, InvoiceStatus invoiceStatus);
}