package com.armaninvestment.parsparandreporter.mappers;

import com.armaninvestment.parsparandreporter.dtos.EstablishmentDto;
import com.armaninvestment.parsparandreporter.entities.Establishment;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface EstablishmentMapper {
    @Mapping(source = "customerId", target = "customer.id")
    Establishment toEntity(EstablishmentDto establishmentDto);

    @Mapping(source = "customer.id", target = "customerId")
    EstablishmentDto toDto(Establishment establishment);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(source = "customerId", target = "customer.id")
    Establishment partialUpdate(EstablishmentDto establishmentDto, @MappingTarget Establishment establishment);
}