package com.armaninvestment.parsparandreporter.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class NotInvoicedDetailsDto {
    private Long receiptNumber;
    private String receiptDate;
    private String receiptDescription;
    private Long receiptAmount;
}
