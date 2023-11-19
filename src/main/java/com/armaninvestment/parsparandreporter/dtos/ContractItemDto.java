package com.armaninvestment.parsparandreporter.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO for {@link com.armaninvestment.parsparandreporter.entities.ContractItem}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContractItemDto implements Serializable {
    private Long id;
    private Long unitPrice;
    private Long quantity;
    private Long productId;
}