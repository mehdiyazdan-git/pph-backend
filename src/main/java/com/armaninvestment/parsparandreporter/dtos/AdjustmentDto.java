package com.armaninvestment.parsparandreporter.dtos;

import com.armaninvestment.parsparandreporter.enums.AdjustmentType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * DTO for {@link com.armaninvestment.parsparandreporter.entities.Adjustment}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AdjustmentDto implements Serializable {
    private Long id;
    private String description;
    private Long adjustmentNumber;
    private LocalDate adjustmentDate;
    private Double unitPrice;
    private Long quantity;
    private AdjustmentType adjustmentType;
    private Long invoiceId;
}