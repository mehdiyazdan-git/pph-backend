package com.armaninvestment.parsparandreporter.controllers;

import com.armaninvestment.parsparandreporter.dtos.dropdowns.ContractDropDownDto;
import com.armaninvestment.parsparandreporter.dtos.dropdowns.CustomerDropDownDto;
import com.armaninvestment.parsparandreporter.dtos.dropdowns.ProductDropDownDto;
import com.armaninvestment.parsparandreporter.dtos.dropdowns.ReceiptDropDownDto;
import com.armaninvestment.parsparandreporter.entities.Customer;
import com.armaninvestment.parsparandreporter.entities.Year;
import com.armaninvestment.parsparandreporter.mappers.dropdowns.ContractDropDownMapper;
import com.armaninvestment.parsparandreporter.mappers.dropdowns.CustomerDropDownMapper;
import com.armaninvestment.parsparandreporter.mappers.dropdowns.ProductDropDownMapper;
import com.armaninvestment.parsparandreporter.mappers.dropdowns.ReceiptDropDownMapper;
import com.armaninvestment.parsparandreporter.repositories.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@CrossOrigin
@RestController
@RequestMapping(path = "/api/drop-down")
public class DropDownController {
    private final ContractDropDownMapper contractDropDownMapper;
    private final CustomerDropDownMapper customerDropDownMapper;
    private final ProductDropDownMapper productDropDownMapper;
    private final ReceiptDropDownMapper receiptDropDownMapper;
    private final ContractRepository contractRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final WarehouseReceiptRepository wareHouseReceiptRepository;
    private final YearRepository yearRepository;

    public DropDownController(
            ContractDropDownMapper contractDropDownMapper,
            CustomerDropDownMapper customerDropDownMapper,
            ProductDropDownMapper productDropDownMapper,
            ReceiptDropDownMapper receiptDropDownMapper,
            ContractRepository contractRepository,
            CustomerRepository customerRepository,
            ProductRepository productRepository,
            WarehouseReceiptRepository wareHouseReceiptRepository, YearRepository yearRepository) {
        this.contractDropDownMapper = contractDropDownMapper;
        this.customerDropDownMapper = customerDropDownMapper;
        this.productDropDownMapper = productDropDownMapper;
        this.receiptDropDownMapper = receiptDropDownMapper;
        this.contractRepository = contractRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.wareHouseReceiptRepository = wareHouseReceiptRepository;
        this.yearRepository = yearRepository;
    }

    @GetMapping("/contracts")
    public ResponseEntity<List<ContractDropDownDto>> getContractDropdown(
            @RequestParam(name = "customerId", required = false) Long customerId) {
        List<ContractDropDownDto> contracts;

        if (customerId != null) {
            contracts = contractRepository.findAllByCustomer(new Customer(customerId))
                    .stream()
                    .map(contractDropDownMapper::toDto)
                    .collect(Collectors.toList());
        } else {
            contracts = contractRepository.findAll()
                    .stream()
                    .map(contractDropDownMapper::toDto)
                    .collect(Collectors.toList());
        }

        return ResponseEntity.ok(contracts);
    }

    @GetMapping("/customers")
    public ResponseEntity<List<CustomerDropDownDto>> getCustomerDropdown() {
        List<CustomerDropDownDto> customers = customerRepository.findAll().stream().map(customerDropDownMapper::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/products")
    public ResponseEntity<List<ProductDropDownDto>> getProductDropdown() {
        List<ProductDropDownDto> products = productRepository.findAll().stream().map(productDropDownMapper::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(products);
    }

    @GetMapping("/receipts/{yearName}")
    public ResponseEntity<List<ReceiptDropDownDto>> getReceiptDropdown(@PathVariable("yearName") Long yearName) {
        Optional<Year> optionalYear = yearRepository.findByYearName(yearName);
        if (optionalYear.isEmpty()) {
            throw new EntityNotFoundException("سال با شناسه " + yearName + " یافت نشد.");
        }
        List<Object[]> results = wareHouseReceiptRepository.getWareHouseReceipts(optionalYear.get().getId());
        List<ReceiptDropDownDto> list = new ArrayList<>();

        for (Object[] result : results) {
            list.add(new ReceiptDropDownDto(
                    (Long) result[0],
                    (String) result[1]
            ));
        }
        return ResponseEntity.ok(list);
    }

    @GetMapping("/receipts/search/{yearName}")
    public ResponseEntity<List<ReceiptDropDownDto>> searchReceiptDropdown(
            @PathVariable("yearName") Long yearName,
            @RequestParam("searchQuery") String searchQuery
    ) {
        Optional<Year> optionalYear = yearRepository.findByYearName(yearName);
        if (optionalYear.isEmpty()) {
            throw new EntityNotFoundException("سال با شناسه " + yearName + " یافت نشد.");
        }
        List<Object[]> results = wareHouseReceiptRepository.searchWarehouseReceiptByDescriptionKeywords(searchQuery, optionalYear.get().getId());
        List<ReceiptDropDownDto> list = new ArrayList<>();

        for (Object[] result : results) {
            list.add(new ReceiptDropDownDto(
                    (Long) result[0],
                    (String) result[1]
            ));
        }
        return ResponseEntity.ok(list);
    }
}
