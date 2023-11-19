package com.armaninvestment.parsparandreporter.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class InvoiceListDto implements Serializable {
    private Long id;
    private Long invoiceNumber;
    private LocalDate issuedDate;
    private LocalDate dueDate;
    private Long contractId;
    private String customerName;
    private String SalesType;
    private String contractNumber;
    private String contractDescription;
    private Integer invoiceStatusId;
    private String invoiceStatusName;
    private Long invoiceTotalAmount;
}