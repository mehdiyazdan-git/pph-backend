package com.armaninvestment.parsparandreporter.mappers;

import com.armaninvestment.parsparandreporter.dtos.ContractDto;
import com.armaninvestment.parsparandreporter.entities.Contract;
import com.armaninvestment.parsparandreporter.entities.ContractItem;
import com.armaninvestment.parsparandreporter.entities.Invoice;

import java.util.Set;


public interface ContractMapper {
    Contract toEntity(ContractDto contractDto);

    default void linkContractItems(Contract contract) {
        contract.getContractItems().forEach(contractItem -> contractItem.setContract(contract));
    }

    ContractDto toDto(Contract contract);

    Contract partialUpdate(ContractDto contractDto, Contract contract);

    default Long calculateCumulativeInvoiceAmount(Set<Invoice> invoices) {
        return invoices.stream().mapToLong(Invoice::getAmount).sum();
    }

    default Long calculateCumulativeInvoiceQuantity(Set<Invoice> invoices) {
        return invoices.stream().mapToLong(Invoice::getQuantity).sum();
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

    default Long calculateRemainingContractQuantityObligations(Contract contract) {
        Long cumulativeContractAmount = calculateCumulativeContractQuantity(contract.getContractItems());
        Long cumulativeInvoiceAmount = calculateCumulativeInvoiceQuantity(contract.getInvoices());
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

    default Long calculateCumulativeContractPerformanceBonds(Contract contract) {
        Long cumulativeInvoiceAmount = calculateCumulativeInvoiceAmount(contract.getInvoices());
        return Math.round((double) ((cumulativeInvoiceAmount) * contract.getPerformanceBond() / 100));
    }

    default Long calculateCumulativeContractInsuranceDeposits(Contract contract) {
        Long cumulativeInvoiceAmount = calculateCumulativeInvoiceAmount(contract.getInvoices());
        return Math.round((double) ((cumulativeInvoiceAmount) * contract.getInsuranceDeposit() / 100));
    }
}