package com.armaninvestment.parsparandreporter.mappers;

import com.armaninvestment.parsparandreporter.dtos.ReturnedDto;
import com.armaninvestment.parsparandreporter.entities.Returned;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface ReturnedMapper {
    @Mapping(source = "customerId", target = "customer.id")
    Returned toEntity(ReturnedDto returnedDto);

    @Mapping(source = "customer.id", target = "customerId")
    ReturnedDto toDto(Returned returned);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(source = "customerId", target = "customer.id")
    Returned partialUpdate(ReturnedDto returnedDto, @MappingTarget Returned returned);
}