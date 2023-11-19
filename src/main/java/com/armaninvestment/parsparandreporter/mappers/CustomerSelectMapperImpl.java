package com.armaninvestment.parsparandreporter.mappers;

import com.armaninvestment.parsparandreporter.dtos.CustomerSelectDto;
import com.armaninvestment.parsparandreporter.entities.Customer;
import org.springframework.stereotype.Component;

@Component
public class CustomerSelectMapperImpl implements CustomerSelectMapper {
    public CustomerSelectMapperImpl() {
    }

    public Customer toEntity(CustomerSelectDto customerSelectDto) {
        if (customerSelectDto == null) {
            return null;
        } else {
            Customer customer = new Customer();
            customer.setId(customerSelectDto.getId());
            customer.setName(customerSelectDto.getName());
            customer.setPhone(customerSelectDto.getPhone());
            customer.setNationalCode(customerSelectDto.getNationalCode());
            customer.setEconomicCode(customerSelectDto.getEconomicCode());
            customer.setCustomerCode(customerSelectDto.getCustomerCode());
            return customer;
        }
    }

    public CustomerSelectDto toDto(Customer customer) {
        if (customer == null) {
            return null;
        } else {
            CustomerSelectDto customerSelectDto = new CustomerSelectDto();
            customerSelectDto.setId(customer.getId());
            customerSelectDto.setName(customer.getName());
            customerSelectDto.setPhone(customer.getPhone());
            customerSelectDto.setNationalCode(customer.getNationalCode());
            customerSelectDto.setEconomicCode(customer.getEconomicCode());
            customerSelectDto.setCustomerCode(customer.getCustomerCode());
            return customerSelectDto;
        }
    }

}
