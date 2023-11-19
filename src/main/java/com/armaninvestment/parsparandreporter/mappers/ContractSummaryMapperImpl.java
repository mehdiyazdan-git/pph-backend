package com.armaninvestment.parsparandreporter.mappers;

import com.armaninvestment.parsparandreporter.dtos.ContractSummaryDto;
import com.armaninvestment.parsparandreporter.entities.Contract;
import org.springframework.stereotype.Component;

@Component
public class ContractSummaryMapperImpl implements ContractSummaryMapper {
    public ContractSummaryMapperImpl() {
    }

    public ContractSummaryDto toDto(Contract contract) {
        if (contract == null) {
            return null;
        } else {
            ContractSummaryDto contractSummaryDto = new ContractSummaryDto();
            contractSummaryDto.setId(contract.getId());
            contractSummaryDto.setContractNumber(contract.getContractNumber());
            contractSummaryDto.setContractDescription(contract.getContractDescription());
            contractSummaryDto.setCumulativeInvoiceAmount(this.calculateCumulativeInvoiceAmount(contract.getInvoices()));
            contractSummaryDto.setCumulativeContractAmount(this.calculateCumulativeContractAmount(contract.getContractItems()));
            contractSummaryDto.setCumulativeContractQuantity(this.calculateCumulativeContractQuantity(contract.getContractItems()));
            contractSummaryDto.setRemainingContractObligations(this.calculateRemainingContractObligations(contract));
            contractSummaryDto.setConsumedAdvancePayment(this.calculateConsumedAdvancePayment(contract));
            contractSummaryDto.setOutstandingAdvancePayment(this.calculateOutstandingAdvancePayment(contract));

            return contractSummaryDto;
        }
    }
}

