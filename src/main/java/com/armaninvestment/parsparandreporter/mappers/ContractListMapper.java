package com.armaninvestment.parsparandreporter.mappers;

import com.armaninvestment.parsparandreporter.dtos.ContractListDto;
import com.armaninvestment.parsparandreporter.entities.Contract;

public interface ContractListMapper {
    ContractListDto toDto(Contract contract);
}