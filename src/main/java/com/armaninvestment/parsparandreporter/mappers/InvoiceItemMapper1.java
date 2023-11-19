package com.armaninvestment.parsparandreporter.mappers;

import com.armaninvestment.parsparandreporter.dtos.InvoiceItemDto1;
import com.armaninvestment.parsparandreporter.entities.InvoiceItem;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface InvoiceItemMapper1 {
    @Mapping(source = "warehouseReceiptId", target = "warehouseReceipt.id")
    @Mapping(source = "productId", target = "product.id")
    InvoiceItem toEntity(InvoiceItemDto1 invoiceItemDto1);

    @InheritInverseConfiguration(name = "toEntity")
    InvoiceItemDto1 toDto(InvoiceItem invoiceItem);

    @InheritConfiguration(name = "toEntity")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    InvoiceItem partialUpdate(InvoiceItemDto1 invoiceItemDto1, @MappingTarget InvoiceItem invoiceItem);
}