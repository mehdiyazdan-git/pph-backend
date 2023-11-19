package com.armaninvestment.parsparandreporter.mappers.dropdowns;

import com.armaninvestment.parsparandreporter.dtos.dropdowns.ContractDropDownDto;
import com.armaninvestment.parsparandreporter.entities.Contract;
import org.springframework.stereotype.Component;

@Component
public class ContractDropDownMapperImpl implements ContractDropDownMapper {
    public ContractDropDownMapperImpl() {
    }

    public ContractDropDownDto toDto(Contract contract) {
        if (contract == null) {
            return null;
        } else {
            ContractDropDownDto contractDropDownDto = new ContractDropDownDto();
            contractDropDownDto.setId(contract.getId());
            contractDropDownDto.setContractNumber(contract.getContractNumber());
            return contractDropDownDto;
        }
    }
}
