package com.armaninvestment.parsparandreporter.mappers;

import com.armaninvestment.parsparandreporter.dtos.ContractDto;
import com.armaninvestment.parsparandreporter.entities.Contract;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring", uses = {ContractItemMapper.class})
public interface ContractMapper {
    @Mapping(source = "yearId", target = "year.id")
    @Mapping(source = "customerId", target = "customer.id")
    Contract toEntity(ContractDto contractDto);

    @AfterMapping
    default void linkContractItems(@MappingTarget Contract contract) {
        contract.getContractItems().forEach(contractItem -> contractItem.setContract(contract));
    }

    @InheritInverseConfiguration(name = "toEntity")
    ContractDto toDto(Contract contract);

    @InheritConfiguration(name = "toEntity")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Contract partialUpdate(ContractDto contractDto, @MappingTarget Contract contract);
}