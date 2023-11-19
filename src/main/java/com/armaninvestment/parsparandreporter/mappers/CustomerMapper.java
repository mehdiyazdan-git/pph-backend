package com.armaninvestment.parsparandreporter.mappers;

import com.armaninvestment.parsparandreporter.dtos.CustomerDto;
import com.armaninvestment.parsparandreporter.entities.Customer;


public interface CustomerMapper {
    Customer toEntity(CustomerDto customerDto);

    default void linkPayments(Customer customer) {
        customer.getPayments().forEach(payment -> payment.setCustomer(customer));
    }

    CustomerDto toDto(Customer customer);

    Customer partialUpdate(CustomerDto customerDto, Customer customer);
}