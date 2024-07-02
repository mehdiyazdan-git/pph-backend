package com.armaninvestment.parsparandreporter.mappers;

import com.armaninvestment.parsparandreporter.dtos.CustomerDto;
import com.armaninvestment.parsparandreporter.entities.Customer;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapperImpl implements CustomerMapper {

    public Customer toEntity(CustomerDto customerDto) {
        if (customerDto == null) {
            return null;
        } else {
            Customer customer = new Customer();
            customer.setId(customerDto.getId());
            customer.setName(customerDto.getName());
            customer.setPhone(customerDto.getPhone());
            customer.setCustomerCode(customerDto.getCustomerCode());
            customer.setEconomicCode(customerDto.getEconomicCode());
            customer.setNationalCode(customerDto.getNationalCode());
            customer.setBigCustomer(customerDto.getBigCustomer());
            this.linkPayments(customer);
            return customer;
        }
    }

    public CustomerDto toDto(Customer customer) {
        if (customer == null) {
            return null;
        } else {
            CustomerDto customerDto = new CustomerDto();
            customerDto.setId(customer.getId());
            customerDto.setName(customer.getName());
            customerDto.setPhone(customer.getPhone());
            customerDto.setCustomerCode(customer.getCustomerCode());
            customerDto.setEconomicCode(customer.getEconomicCode());
            customerDto.setNationalCode(customer.getNationalCode());
            customerDto.setBigCustomer(customer.getBigCustomer());
            return customerDto;
        }
    }

    public Customer partialUpdate(CustomerDto customerDto, Customer customer) {
        if (customerDto == null) {
            return null;
        } else {
            if (customerDto.getId() != null) {
                customer.setId(customerDto.getId());
            }

            if (customerDto.getName() != null) {
                customer.setName(customerDto.getName());
            }

            if (customerDto.getPhone() != null) {
                customer.setPhone(customerDto.getPhone());
            }

            if (customerDto.getCustomerCode() != null) {
                customer.setCustomerCode(customerDto.getCustomerCode());
            }

            if (customerDto.getEconomicCode() != null) {
                customer.setEconomicCode(customerDto.getEconomicCode());
            }

            if (customerDto.getNationalCode() != null) {
                customer.setNationalCode(customerDto.getNationalCode());
            }
            customer.setBigCustomer(customerDto.getBigCustomer());
            return customer;
        }
    }
}
