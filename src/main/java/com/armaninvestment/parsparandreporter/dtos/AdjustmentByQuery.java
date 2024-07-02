package com.armaninvestment.parsparandreporter.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AdjustmentByQuery {
    private Long adjustmentId;
    private String adjustmentType;
    private String adjustmentDescription;
    private Long adjustmentQuantity;
    private Double adjustmentUnitPrice;
    private Double amount;
    private Long invoiceId;
    private LocalDate adjustmentDate;
    private Long adjustmentNumber;
    private Long invoiceNumber;
}
