package com.armaninvestment.parsparandreporter.dtos.dropdowns;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO for {@link com.armaninvestment.parsparandreporter.entities.Customer}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerDropDownDto implements Serializable {
    private Long id;
    private String name;
}