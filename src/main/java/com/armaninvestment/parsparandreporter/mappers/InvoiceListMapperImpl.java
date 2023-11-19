package com.armaninvestment.parsparandreporter.mappers;

import com.armaninvestment.parsparandreporter.dtos.InvoiceListDto;
import com.armaninvestment.parsparandreporter.entities.*;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class InvoiceListMapperImpl implements InvoiceListMapper {
    public InvoiceListMapperImpl() {
    }


    public InvoiceListDto toDto(Invoice invoice) {
        if (invoice == null) {
            return null;
        } else {
            InvoiceListDto invoiceListDto = new InvoiceListDto();
            invoiceListDto.setInvoiceStatusName(this.getStatusName(invoice));
            invoiceListDto.setInvoiceStatusId(this.getStatusId(invoice));
            invoiceListDto.setContractDescription(this.getContractDescription(invoice));
            invoiceListDto.setContractNumber(this.getContractNumber(invoice));
            invoiceListDto.setContractId(this.getContractId(invoice));
            invoiceListDto.setCustomerName(this.getCustomerName(invoice));
            invoiceListDto.setSalesType(invoice.getSalesType().getPersianCaption());
            invoiceListDto.setId(invoice.getId());
            invoiceListDto.setInvoiceNumber(invoice.getInvoiceNumber());
            invoiceListDto.setIssuedDate(invoice.getIssuedDate());
            invoiceListDto.setDueDate(invoice.getDueDate());
            invoiceListDto.setInvoiceTotalAmount(this.calculateTotalAmount(invoice));
            return invoiceListDto;
        }
    }

    private String getStatusName(Invoice invoice) {
        if (invoice == null) {
            return null;
        } else {
            InvoiceStatus invoiceStatus = invoice.getInvoiceStatus();
            if (invoiceStatus == null) {
                return null;
            } else {
                return invoiceStatus.getName();
            }
        }
    }

    private String getCustomerName(Invoice invoice) {
        if (invoice == null) {
            return null;
        } else {
            Customer customer = invoice.getCustomer();
            if (customer == null) {
                return null;
            } else {
                return customer.getName();
            }
        }
    }

    private Integer getStatusId(Invoice invoice) {
        if (invoice == null) {
            return null;
        } else {
            InvoiceStatus invoiceStatus = invoice.getInvoiceStatus();
            if (invoiceStatus == null) {
                return null;
            } else {
                return invoiceStatus.getId();
            }
        }
    }

    private String getContractDescription(Invoice invoice) {
        if (invoice == null) {
            return null;
        } else {
            Contract contract = invoice.getContract();
            if (contract == null) {
                return null;
            } else {
                return contract.getContractDescription();
            }
        }
    }

    private String getContractNumber(Invoice invoice) {
        if (invoice == null) {
            return null;
        } else {
            Contract contract = invoice.getContract();
            if (contract == null) {
                return null;
            } else {
                return contract.getContractNumber();
            }
        }
    }

    private Long getContractId(Invoice invoice) {
        if (invoice == null) {
            return null;
        } else {
            Contract contract = invoice.getContract();
            if (contract == null) {
                return null;
            } else {
                return contract.getId();
            }
        }
    }

    private Long calculateTotalAmount(Invoice invoice) {
        if (invoice == null) {
            return 0L;
        } else {
            Set<InvoiceItem> invoiceItems = invoice.getInvoiceItems();
            if (invoiceItems == null) {
                return 0L;
            } else {
                long totalAmount = 0L;
                for (InvoiceItem item : invoiceItems) {
                    totalAmount = totalAmount + (item.getQuantity() * item.getUnitPrice());
                }
                return totalAmount;
            }
        }
    }
}
