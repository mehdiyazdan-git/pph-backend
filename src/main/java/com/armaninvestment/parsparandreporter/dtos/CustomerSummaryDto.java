package com.armaninvestment.parsparandreporter.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CustomerSummaryDto {
    private Long id;
    private String customerName;
    private Long issuedInvoicesCount;
    private Long issuedInvoiceAmount;
    private Long notIssuedInvoicesCount;
    private Long notIssuedInvoiceAmount;
}
