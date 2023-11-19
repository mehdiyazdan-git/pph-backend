package com.armaninvestment.parsparandreporter.mappers;

import com.armaninvestment.parsparandreporter.dtos.InvoiceStatusDto;
import com.armaninvestment.parsparandreporter.entities.InvoiceStatus;
import org.springframework.stereotype.Component;

@Component
public class InvoiceStatusMapperImpl implements InvoiceStatusMapper {
    public InvoiceStatusMapperImpl() {
    }

    public InvoiceStatus toEntity(InvoiceStatusDto invoiceStatusDto) {
        if (invoiceStatusDto == null) {
            return null;
        } else {
            InvoiceStatus invoiceStatus = new InvoiceStatus();
            invoiceStatus.setId(invoiceStatusDto.getId());
            invoiceStatus.setName(invoiceStatusDto.getName());
            return invoiceStatus;
        }
    }

    public InvoiceStatusDto toDto(InvoiceStatus invoiceStatus) {
        if (invoiceStatus == null) {
            return null;
        } else {
            InvoiceStatusDto invoiceStatusDto = new InvoiceStatusDto();
            invoiceStatusDto.setId(invoiceStatus.getId());
            invoiceStatusDto.setName(invoiceStatus.getName());
            return invoiceStatusDto;
        }
    }

    public InvoiceStatus partialUpdate(InvoiceStatusDto invoiceStatusDto, InvoiceStatus invoiceStatus) {
        if (invoiceStatusDto == null) {
            return null;
        } else {
            if (invoiceStatusDto.getId() != null) {
                invoiceStatus.setId(invoiceStatusDto.getId());
            }

            if (invoiceStatusDto.getName() != null) {
                invoiceStatus.setName(invoiceStatusDto.getName());
            }

            return invoiceStatus;
        }
    }
}
