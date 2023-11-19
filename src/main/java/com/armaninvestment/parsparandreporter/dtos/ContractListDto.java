package com.armaninvestment.parsparandreporter.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContractListDto implements Serializable {
    private Long id;
    private String contractNumber;
    private String contractDescription;
    private LocalDate startDate;
    private LocalDate endDate;
    private String customerName;
    private Long totalAmount;
    private Long totalQuantity;
    private Long invoiceTotalAmount;

    public ContractListDto(Long id,
                           String contractNumber,
                           String contractDescription,
                           String customerName,
                           LocalDate startDate,
                           LocalDate endDate) {
        this.id = id;
        this.contractNumber = contractNumber;
        this.contractDescription = contractDescription;
        this.customerName = customerName;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}