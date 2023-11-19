package com.armaninvestment.parsparandreporter.mappers.dropdowns;

import com.armaninvestment.parsparandreporter.dtos.dropdowns.CustomerDropDownDto;
import com.armaninvestment.parsparandreporter.entities.Customer;
import org.springframework.stereotype.Component;

@Component
public class CustomerDropDownMapperImpl implements CustomerDropDownMapper {
    public CustomerDropDownMapperImpl() {
    }

    public CustomerDropDownDto toDto(Customer customer) {
        if (customer == null) {
            return null;
        } else {
            CustomerDropDownDto customerDropDownDto = new CustomerDropDownDto();
            customerDropDownDto.setId(customer.getId());
            customerDropDownDto.setName(customer.getName());
            return customerDropDownDto;
        }
    }
}
