package com.armaninvestment.parsparandreporter.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddendumDto implements Serializable {
    private Long id;
    private String addendumNumber;
    private Long contractId;
    private Long unitPrice;
    private Long quantity;
    private LocalDate startDate;
    private LocalDate endDate;
}