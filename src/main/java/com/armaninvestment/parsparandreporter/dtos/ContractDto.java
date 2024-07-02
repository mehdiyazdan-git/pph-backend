package com.armaninvestment.parsparandreporter.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * DTO for {@link com.armaninvestment.parsparandreporter.entities.Contract}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContractDto implements Serializable {
    private Long id;
    private String contractNumber;
    private String contractDescription;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double advancePayment;
    private Double performanceBond;
    private Double insuranceDeposit;
    private Long customerId;
    private Set<ContractItemDto> contractItems = new LinkedHashSet<>();
    private Long yearId;
}