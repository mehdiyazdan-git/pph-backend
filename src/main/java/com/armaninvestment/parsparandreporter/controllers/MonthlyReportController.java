package com.armaninvestment.parsparandreporter.controllers;

import com.armaninvestment.parsparandreporter.dtos.CompanyReportDTO;
import com.armaninvestment.parsparandreporter.repositories.MonthlyReportByYearAndMonthRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;

@CrossOrigin
@RestController
@RequestMapping("/api/monthly-report")
public class MonthlyReportController {


    private final MonthlyReportByYearAndMonthRepository monthlyReportRepo;

    @Autowired
    public MonthlyReportController(MonthlyReportByYearAndMonthRepository monthlyReportRepo) {
        this.monthlyReportRepo = monthlyReportRepo;
    }

//    @GetMapping("/{year}/{month}")
//    public ResponseEntity<List<CompanyReportDTO>> getMonthlyReport(@PathVariable int year, @PathVariable int month) {
//        List<CompanyReportDTO> report = monthlyReportRepo.getReport(year, month);
//        return ResponseEntity.ok(report);
//    }

    @GetMapping("/by-product/{year}/{month}/{productType}")
    public ResponseEntity<List<CompanyReportDTO>> getMonthlyReportByProduct(@PathVariable int year, @PathVariable int month, @PathVariable String productType) {
        List<CompanyReportDTO> report = monthlyReportRepo.getReport(year, month, productType);

        // Filter the report list to include only records where bigCustomer is false
        List<CompanyReportDTO> nonBigCustomers = report.stream()
                .filter(dto -> !dto.getBigCustomer())
                .toList();

        // Calculate the total sum of the fields you want
        long totalQuantity = nonBigCustomers.stream()
                .mapToLong(CompanyReportDTO::getQuantity)
                .sum();
        OptionalDouble average = nonBigCustomers.stream()
                .mapToDouble(CompanyReportDTO::getAvg_unit_price) // Use mapToDouble here
                .average();
        long totalAmount = nonBigCustomers.stream()
                .mapToLong(CompanyReportDTO::getAmount)
                .sum();
        long totalCumulativeQuantity = nonBigCustomers.stream()
                .mapToLong(CompanyReportDTO::getCumulative_quantity)
                .sum();

        // Calculate the average as a double and then cast it to a long if needed
        long totalAvgUnitPrice = average.isPresent() ? (long) average.getAsDouble() : 0L; // Use a default value if the average is not present

        // Create a new CompanyReportDTO with the summed values
        CompanyReportDTO totalReport = new CompanyReportDTO(
                100000L, // You may set an appropriate ID or leave it as null
                "سایر",
                false, // Provide an appropriate name for the total entry
                totalQuantity,
                totalAvgUnitPrice,
                totalAmount,
                totalCumulativeQuantity
        );
        List<CompanyReportDTO> bigCustomers = new ArrayList<>(report.stream()
                .filter(CompanyReportDTO::getBigCustomer)
                .toList());
        bigCustomers.add(totalReport);

        return ResponseEntity.ok(bigCustomers);
    }


    @GetMapping("/{year}/{month}/{productType}")
    public ResponseEntity<List<CompanyReportDTO>> getMonthlyReport(@PathVariable int year, @PathVariable int month, @PathVariable String productType) {


        return ResponseEntity.ok(monthlyReportRepo.getReport(year, month, productType));
    }
}

