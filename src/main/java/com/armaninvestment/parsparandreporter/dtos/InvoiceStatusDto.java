package com.armaninvestment.parsparandreporter.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO for {@link com.armaninvestment.parsparandreporter.entities.InvoiceStatus}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceStatusDto implements Serializable {
    private Integer id;
    private String name;
}