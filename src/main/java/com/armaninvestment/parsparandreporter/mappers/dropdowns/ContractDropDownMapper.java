package com.armaninvestment.parsparandreporter.mappers.dropdowns;

import com.armaninvestment.parsparandreporter.dtos.dropdowns.ContractDropDownDto;
import com.armaninvestment.parsparandreporter.entities.Contract;


public interface ContractDropDownMapper {
    ContractDropDownDto toDto(Contract contract);
}