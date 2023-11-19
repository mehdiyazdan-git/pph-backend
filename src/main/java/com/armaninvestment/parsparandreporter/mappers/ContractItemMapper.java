package com.armaninvestment.parsparandreporter.mappers;

import com.armaninvestment.parsparandreporter.dtos.ContractItemDto;
import com.armaninvestment.parsparandreporter.entities.ContractItem;

public interface ContractItemMapper {
    ContractItem toEntity(ContractItemDto contractItemDto);

    ContractItemDto toDto(ContractItem contractItem);

    ContractItem partialUpdate(ContractItemDto contractItemDto, ContractItem contractItem);
}