package com.armaninvestment.parsparandreporter.mappers;

import com.armaninvestment.parsparandreporter.dtos.InvoiceDto1;
import com.armaninvestment.parsparandreporter.entities.Invoice;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring", uses = {InvoiceItemMapper1.class})
public interface InvoiceMapper1 {
    @Mapping(source = "yearId", target = "year.id")
    @Mapping(source = "invoiceStatusId", target = "invoiceStatus.id")
    @Mapping(source = "customerId", target = "customer.id")
    @Mapping(source = "contractId", target = "contract.id")
    Invoice toEntity(InvoiceDto1 invoiceDto1);

    @AfterMapping
    default void linkInvoiceItems(@MappingTarget Invoice invoice) {
        invoice.getInvoiceItems().forEach(invoiceItem -> invoiceItem.setInvoice(invoice));
    }

    @InheritInverseConfiguration(name = "toEntity")
    InvoiceDto1 toDto(Invoice invoice);

    @InheritConfiguration(name = "toEntity")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Invoice partialUpdate(InvoiceDto1 invoiceDto1, @MappingTarget Invoice invoice);
}