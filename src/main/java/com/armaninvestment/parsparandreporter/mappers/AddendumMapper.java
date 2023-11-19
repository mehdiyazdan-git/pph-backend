package com.armaninvestment.parsparandreporter.mappers;

import com.armaninvestment.parsparandreporter.dtos.AddendumDto;
import com.armaninvestment.parsparandreporter.entities.Addendum;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface AddendumMapper {
    @Mapping(source = "contractId", target = "contract.id")
    Addendum toEntity(AddendumDto addendumDto);

    @Mapping(source = "contract.id", target = "contractId")
    AddendumDto toDto(Addendum addendum);

    @Mapping(source = "contractId", target = "contract.id")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Addendum partialUpdate(AddendumDto addendumDto, @MappingTarget Addendum addendum);
}
