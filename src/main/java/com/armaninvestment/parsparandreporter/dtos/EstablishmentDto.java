package com.armaninvestment.parsparandreporter.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO for {@link com.armaninvestment.parsparandreporter.entities.Establishment}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class EstablishmentDto implements Serializable {
    private Long id;
    private Double claims;
    private Double periorInsuranceDeposit;
    private Double periorPerformanceBound;
    private Long customerId;
}