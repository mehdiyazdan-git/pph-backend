package com.armaninvestment.parsparandreporter.dtos.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AdjustmentReportDto {
    private Double amount;
    private Double vat;
    private Long insurance;
    private Long performance;
}
