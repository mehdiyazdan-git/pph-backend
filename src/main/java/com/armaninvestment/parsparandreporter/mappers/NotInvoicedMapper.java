package com.armaninvestment.parsparandreporter.mappers;

import com.armaninvestment.parsparandreporter.dtos.NotInvoiced;
import com.armaninvestment.parsparandreporter.dtos.NotInvoicedDto;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface NotInvoicedMapper {
    NotInvoiced toEntity(NotInvoicedDto notInvoicedDto);

    NotInvoicedDto toDto(NotInvoiced notInvoiced);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    NotInvoiced partialUpdate(NotInvoicedDto notInvoicedDto, @MappingTarget NotInvoiced notInvoiced);
}