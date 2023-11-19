package com.armaninvestment.parsparandreporter.controllers;

import com.armaninvestment.parsparandreporter.dtos.CustomerSummaryDto;
import com.armaninvestment.parsparandreporter.services.CustomerSummaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping(path = "/api/customers/customer-summary")
public class CustomerSummaryController {
    private final CustomerSummaryService customerSummaryService;

    @Autowired
    public CustomerSummaryController(CustomerSummaryService customerSummaryService) {
        this.customerSummaryService = customerSummaryService;
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<CustomerSummaryDto> findById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(customerSummaryService.findById(id));
    }
}
