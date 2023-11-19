package com.armaninvestment.parsparandreporter.services;

import com.armaninvestment.parsparandreporter.dtos.CustomerSummaryDto;
import com.armaninvestment.parsparandreporter.entities.Customer;
import com.armaninvestment.parsparandreporter.mappers.CustomerSummaryMapper;
import com.armaninvestment.parsparandreporter.repositories.CustomerRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomerSummaryService {
    private final CustomerRepository customerRepository;
    private final CustomerSummaryMapper customerSummaryMapper;

    @Autowired
    public CustomerSummaryService(CustomerRepository customerRepository, CustomerSummaryMapper customerSummaryMapper) {
        this.customerRepository = customerRepository;
        this.customerSummaryMapper = customerSummaryMapper;
    }


    public CustomerSummaryDto findById(Long customerId) {
        Optional<Customer> optionalCustomer = customerRepository.findById(customerId);
        return customerSummaryMapper.toDto(optionalCustomer.orElseThrow(() -> new EntityNotFoundException("مشتری با شناسه " + customerId + "یافت نشد.")));
    }
}
