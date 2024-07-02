package com.armaninvestment.parsparandreporter.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * DTO for {@link com.armaninvestment.parsparandreporter.entities.Payment}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentDto implements Serializable {
    private Long id;
    private String description;
    private LocalDate date;
    private Long amount;
    private String subject = "PRODUCT";
    private Long customerId;
    private Long yearId;
}