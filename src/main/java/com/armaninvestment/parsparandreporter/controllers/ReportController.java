package com.armaninvestment.parsparandreporter.controllers;


import com.armaninvestment.parsparandreporter.dtos.ReportDto;
import com.armaninvestment.parsparandreporter.dtos.ReportWithSubtotalDTO;
import com.armaninvestment.parsparandreporter.dtos.SalesByYearGroupByMonth;
import com.armaninvestment.parsparandreporter.entities.Report;
import com.armaninvestment.parsparandreporter.exceptions.DatabaseIntegrityViolationException;
import com.armaninvestment.parsparandreporter.exceptions.RowImportException;
import com.armaninvestment.parsparandreporter.exceptions.WarehouseReceiptAlreadyAssociatedException;
import com.armaninvestment.parsparandreporter.mappers.ReportMapper;
import com.armaninvestment.parsparandreporter.repositories.ReportRepository;
import com.armaninvestment.parsparandreporter.services.ReportService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@CrossOrigin
@RestController
@RequestMapping(path = "/api/reports")
public class ReportController {
    private final ReportRepository repository;
    private final ReportService reportService;
    private final ReportMapper mapper;

    @Autowired
    public ReportController(ReportRepository repository, ReportService reportService, ReportMapper mapper) {
        this.repository = repository;
        this.reportService = reportService;
        this.mapper = mapper;
    }

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> getReportsWithSubtotals(
            @RequestParam(defaultValue = "0") Integer pageNo,
            @RequestParam(defaultValue = "5") Integer pageSize,
            @RequestParam(defaultValue = "date") String sortBy,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate
    ) {
        Page<ReportWithSubtotalDTO> reportPage = reportService.getReportsWithSubtotals(pageNo, pageSize, sortBy, startDate, endDate);

        Map<String, Object> response = new HashMap<>();
        response.put("reports", reportPage.getContent());
        response.put("totalPages", reportPage.getTotalPages());

        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/by-year/{yearId}")
    public ResponseEntity<List<ReportWithSubtotalDTO>> getAllReportsByYearId(@PathVariable("yearId") Long yearId) {
        return ResponseEntity.ok(reportService.getAllReportsByYearId(yearId));
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<ReportDto> findById(@PathVariable("id") Long id) {
        Optional<Report> optionalReport = repository.findById(id);
        if (optionalReport.isPresent()) {
            Report report = optionalReport.get();
            return ResponseEntity.ok(mapper.toDto(report));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping(path = {"/", ""})
    @Transactional
    public ResponseEntity<?> createReport(@RequestBody ReportDto reportDto) {
        try {
            Report report = mapper.toEntity(reportDto);
            Report savedReport = repository.save(report);
            ReportDto savedReportDto = mapper.toDto(savedReport);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedReportDto);
        } catch (PersistenceException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("خطا در ایجاد: " + e.getMessage());
        } catch (WarehouseReceiptAlreadyAssociatedException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @PutMapping(path = "/{id}")
    @Transactional
    public ResponseEntity<?> updateReport(@PathVariable("id") Long id, @RequestBody ReportDto reportDto) {
        try {
            Optional<Report> optionalReport = repository.findById(id);
            if (optionalReport.isPresent()) {
                Report report = optionalReport.get();
                Report reportFromReportDto = mapper.partialUpdate(reportDto, report);
                Report update = repository.save(reportFromReportDto);
                return ResponseEntity.ok(mapper.toDto(update));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (PersistenceException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("خطا در ایجاد: " + e.getMessage());
        } catch (WarehouseReceiptAlreadyAssociatedException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity<?> deleteReport(@PathVariable("id") Long id) {
        try {
            reportService.deleteReport(id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("قرارداد با موفقیت حذف شد.");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("قرارداد با شناسه " + id + "یافت نشد.");
        } catch (DatabaseIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("خطای سمت سرور...");
        }
    }

    @PostMapping("/import")
    public ResponseEntity<String> importReports(@RequestParam("file") MultipartFile file) {
        try {
            reportService.importReportsFromExcel(file);
            return ResponseEntity.status(HttpStatus.CREATED).body("فایل با موفقیت باگذاری شد.");
        } catch (RowImportException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("خطا در بارگذاری فایل: " + e.getMessage());
        } catch (EntityNotFoundException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("خطا در بارگذاری فایل: " + e.getMessage());
        }
    }

    @GetMapping(path = "/sales-by-year/{yearName}/{productType}")
    public ResponseEntity<List<SalesByYearGroupByMonth>> getSalesByYearGroupByMonth(
            @PathVariable("yearName") Short yearName,
            @PathVariable("productType") String productType) {
        return ResponseEntity.ok(reportService.findSalesByYearGroupByMonth(yearName, productType));
    }

}
