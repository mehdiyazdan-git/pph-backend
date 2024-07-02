package com.armaninvestment.parsparandreporter.controllers;

import com.armaninvestment.parsparandreporter.dtos.CompanyReportDTO;
import com.armaninvestment.parsparandreporter.repositories.MonthlyReportByYearAndMonthRepository;
import com.armaninvestment.parsparandreporter.services.AdjustmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api/monthly-report")
public class MonthlyReportController {


    private final MonthlyReportByYearAndMonthRepository monthlyReportRepo;
    private final AdjustmentService adjustmentService;

    @Autowired
    public MonthlyReportController(MonthlyReportByYearAndMonthRepository monthlyReportRepo, AdjustmentService adjustmentService) {
        this.monthlyReportRepo = monthlyReportRepo;
        this.adjustmentService = adjustmentService;
    }

    @GetMapping("/by-product/{year}/{month}/{productType}")
    public ResponseEntity<List<CompanyReportDTO>> getMonthlyReportByProduct(
            @PathVariable Integer year,
            @PathVariable Integer month,
            @PathVariable String productType) {

        List<Object[]> resultSet = monthlyReportRepo.getReport(year, month, productType);
        List<CompanyReportDTO> list = resultSet.stream().map(obj -> {
            CompanyReportDTO dto = new CompanyReportDTO();
            dto.setId((Long) obj[0]);
            dto.setCustomerName((String) obj[1]);
            dto.setTotalQuantity((Long) obj[2]);
            dto.setTotalAmount((Long) obj[3]);
            dto.setCumulativeTotalQuantity((Long) obj[4]);
            dto.setCumulativeTotalAmount((Long) obj[5]);
            dto.setAvgUnitPrice((Long) obj[6]);
            return dto;
        }).toList();
        return ResponseEntity.ok(list);
    }

}

