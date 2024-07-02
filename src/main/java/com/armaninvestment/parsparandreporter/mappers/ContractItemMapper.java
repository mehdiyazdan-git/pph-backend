package com.armaninvestment.parsparandreporter.mappers;

import com.armaninvestment.parsparandreporter.dtos.ContractItemDto;
import com.armaninvestment.parsparandreporter.entities.ContractItem;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface ContractItemMapper {
    @Mapping(source = "contractId", target = "contract.id")
    @Mapping(source = "productId", target = "product.id")
    ContractItem toEntity(ContractItemDto contractItemDto);

    @InheritInverseConfiguration(name = "toEntity")
    ContractItemDto toDto(ContractItem contractItem);

    @InheritConfiguration(name = "toEntity")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    ContractItem partialUpdate(ContractItemDto contractItemDto, @MappingTarget ContractItem contractItem);
}