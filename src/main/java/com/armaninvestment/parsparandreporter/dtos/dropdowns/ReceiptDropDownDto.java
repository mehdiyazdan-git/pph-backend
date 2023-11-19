package com.armaninvestment.parsparandreporter.dtos.dropdowns;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO for {@link com.armaninvestment.parsparandreporter.entities.WarehouseReceipt}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReceiptDropDownDto implements Serializable {
    private Long id;
    private String desc;
}