package com.armaninvestment.parsparandreporter.mappers;

import com.armaninvestment.parsparandreporter.dtos.WarehouseInvoiceDto;
import com.armaninvestment.parsparandreporter.entities.WarehouseInvoice;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface WarehouseInvoiceMapper {
    WarehouseInvoice toEntity(WarehouseInvoiceDto warehouseInvoiceDto);

    WarehouseInvoiceDto toDto(WarehouseInvoice warehouseInvoice);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    WarehouseInvoice partialUpdate(WarehouseInvoiceDto warehouseInvoiceDto, @MappingTarget WarehouseInvoice warehouseInvoice);
}