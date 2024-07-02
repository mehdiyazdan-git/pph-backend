package com.armaninvestment.parsparandreporter.dtos;

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
public class CustomerDto implements Serializable {
    private Long id;
    private String name;
    private String phone;
    private String customerCode;
    private String economicCode;
    private String nationalCode;
    private boolean bigCustomer;

    public boolean getBigCustomer() {
        return bigCustomer;
    }

    public void setBigCustomer(boolean bigCustomer) {
        this.bigCustomer = bigCustomer;
    }
}