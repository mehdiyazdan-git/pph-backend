package com.armaninvestment.parsparandreporter.mappers.dropdowns;

import com.armaninvestment.parsparandreporter.dtos.dropdowns.CustomerDropDownDto;
import com.armaninvestment.parsparandreporter.entities.Customer;

public interface CustomerDropDownMapper {
    CustomerDropDownDto toDto(Customer customer);
}
