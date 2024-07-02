package com.armaninvestment.parsparandreporter.mappers;

import com.armaninvestment.parsparandreporter.dtos.ContractListDto;
import com.armaninvestment.parsparandreporter.entities.*;
import org.springframework.stereotype.Component;

@Component
public class ContractListMapperImpl implements ContractListMapper {
    public ContractListMapperImpl() {
    }

    public ContractListDto toDto(Contract contract) {
        if (contract == null) {
            return null;
        } else {
            ContractListDto contractListDto = new ContractListDto();
            contractListDto.setCustomerName(this.contractCustomerName(contract));
            contractListDto.setId(contract.getId());
            contractListDto.setContractNumber(contract.getContractNumber());
            contractListDto.setContractDescription(contract.getContractDescription());


            long totalAmount = 0;
            long totalQuantity = 0;

            for (ContractItem contractItem : contract.getContractItems()) {
                if (contractItem != null) {
                    Long unitPrice = contractItem.getUnitPrice();
                    Long quantity = contractItem.getQuantity();
                    if (unitPrice != null && quantity != null) {
                        totalAmount += unitPrice * quantity;
                        totalQuantity += quantity;
                    }
                }
            }



            long invoiceTotalAmount = 0;

            for (Invoice invoice : contract.getInvoices()) {
                for (InvoiceItem invoiceItem : invoice.getInvoiceItems()) {
                    if (invoiceItem != null) {
                        Integer quantity = invoiceItem.getQuantity();
                        Long unitPrice = invoiceItem.getUnitPrice();
                        if (quantity != null && unitPrice != null) {
                            invoiceTotalAmount += quantity * unitPrice;
                        }
                    }
                }
            }


            return contractListDto;
        }
    }

    private String contractCustomerName(Contract contract) {
        if (contract == null) {
            return null;
        } else {
            Customer customer = contract.getCustomer();
            if (customer == null) {
                return null;
            } else {
                return customer.getName();
            }
        }
    }
}
