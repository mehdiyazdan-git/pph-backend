package com.armaninvestment.parsparandreporter.mappers;

import com.armaninvestment.parsparandreporter.dtos.ReportItemDto;
import com.armaninvestment.parsparandreporter.entities.ReportItem;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface ReportItemMapper {
    @Mapping(source = "warehouseReceiptId", target = "warehouseReceipt.id")
    @Mapping(source = "reportId", target = "report.id")
    @Mapping(source = "customerId", target = "customer.id")
    ReportItem toEntity(ReportItemDto reportItemDto);

    @InheritInverseConfiguration(name = "toEntity")
    ReportItemDto toDto(ReportItem reportItem);

    @InheritConfiguration(name = "toEntity")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    ReportItem partialUpdate(ReportItemDto reportItemDto, @MappingTarget ReportItem reportItem);
}