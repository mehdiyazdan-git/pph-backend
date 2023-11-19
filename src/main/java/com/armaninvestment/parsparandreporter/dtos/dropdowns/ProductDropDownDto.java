package com.armaninvestment.parsparandreporter.dtos.dropdowns;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO for {@link com.armaninvestment.parsparandreporter.entities.Product}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductDropDownDto implements Serializable {
    private Long id;
    private String productName;
}