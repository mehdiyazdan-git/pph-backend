package com.armaninvestment.parsparandreporter.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class InvoiceFilterDto {
    private Long invoiceNumber;
    private LocalDate issuedDate;
    private LocalDate dueDate;
    private String contractContractDescription;
    private Integer invoiceStatusId;
}
