package com.armaninvestment.parsparandreporter.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ReturnedByQuery {
    private Long returnedId;
    private Long returnedNumber;
    private String returnedDescription;
    private LocalDate returnedDate;
    private Long returnedQuantity;
    private Double returnedUnitPrice;
    private Double returnedAmount;
    private String returnedCustomerName;
}
