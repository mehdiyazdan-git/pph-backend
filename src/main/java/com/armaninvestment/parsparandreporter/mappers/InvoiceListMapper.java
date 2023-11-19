package com.armaninvestment.parsparandreporter.mappers;

import com.armaninvestment.parsparandreporter.dtos.InvoiceListDto;
import com.armaninvestment.parsparandreporter.entities.Invoice;


public interface InvoiceListMapper {
    InvoiceListDto toDto(Invoice invoice);
}