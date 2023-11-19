package com.armaninvestment.parsparandreporter.mappers;

import com.armaninvestment.parsparandreporter.dtos.ContractSummaryDto;
import com.armaninvestment.parsparandreporter.entities.Contract;
import com.armaninvestment.parsparandreporter.entities.ContractItem;
import com.armaninvestment.parsparandreporter.entities.Invoice;

import java.util.Set;

public interface ContractSummaryMapper {
    ContractSummaryDto toDto(Contract contract);

    default Long calculateCumulativeInvoiceAmount(Set<Invoice> invoices) {
        return invoices.stream().mapToLong(Invoice::getAmount).sum();
    }

    default Long calculateCumulativeContractAmount(Set<ContractItem> contractItems) {
        return contractItems.stream().mapToLong(ci -> ci.getUnitPrice() * ci.getQuantity()).sum();
    }

    default Long calculateCumulativeContractQuantity(Set<ContractItem> contractItems) {
        return contractItems.stream().mapToLong(ContractItem::getQuantity).sum();
    }

    default Long calculateRemainingContractObligations(Contract contract) {
        Long cumulativeContractAmount = calculateCumulativeContractAmount(contract.getContractItems());
        Long cumulativeInvoiceAmount = calculateCumulativeInvoiceAmount(contract.getInvoices());
        return cumulativeContractAmount - cumulativeInvoiceAmount;
    }

    default Long calculateConsumedAdvancePayment(Contract contract) {
        Long cumulativeInvoiceAmount = calculateCumulativeInvoiceAmount(contract.getInvoices());
        return Math.round((double) ((cumulativeInvoiceAmount) * contract.getAdvancePayment() / 100));
    }

    default Long calculateOutstandingAdvancePayment(Contract contract) {
        Long cumulativeContractAmount = calculateCumulativeContractAmount(contract.getContractItems());
        Long cumulativeInvoiceAmount = calculateCumulativeInvoiceAmount(contract.getInvoices());
        return Math.round((double) ((cumulativeContractAmount - cumulativeInvoiceAmount) * contract.getAdvancePayment() / 100));
    }
}