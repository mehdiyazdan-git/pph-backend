package com.armaninvestment.parsparandreporter.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * DTO for {@link com.armaninvestment.parsparandreporter.entities.Returned}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReturnedDto implements Serializable {
    private Long id;
    private Long returnedNumber;
    private String returnedDescription;
    private LocalDate returnedDate;
    private Long quantity;
    private Double unitPrice;
    private Long customerId;

    @Override
    public String toString() {
        return "ReturnedDto{" +
                "id=" + id +
                ", returnedNumber=" + returnedNumber +
                ", returnedDescription='" + returnedDescription + '\'' +
                ", returnedDate=" + returnedDate +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                ", customerId=" + customerId +
                '}';
    }
}