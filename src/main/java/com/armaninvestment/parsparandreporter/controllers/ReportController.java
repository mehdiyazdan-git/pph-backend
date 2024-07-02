package com.armaninvestment.parsparandreporter.controllers;


import com.armaninvestment.parsparandreporter.dtos.ReportDto;
import com.armaninvestment.parsparandreporter.dtos.ReportWithSubtotalDTO;
import com.armaninvestment.parsparandreporter.dtos.SalesByYearGroupByMonth;
import com.armaninvestment.parsparandreporter.entities.Report;
import com.armaninvestment.parsparandreporter.entities.ReportDtoByQuery;
import com.armaninvestment.parsparandreporter.entities.Year;
import com.armaninvestment.parsparandreporter.exceptions.*;
import com.armaninvestment.parsparandreporter.mappers.ReportMapper;
import com.armaninvestment.parsparandreporter.repositories.CustomerRepository;
import com.armaninvestment.parsparandreporter.repositories.ReportRepository;
import com.armaninvestment.parsparandreporter.repositories.WarehouseReceiptRepository;
import com.armaninvestment.parsparandreporter.repositories.YearRepository;
import com.armaninvestment.parsparandreporter.services.ReportService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final YearRepository yearRepository;
    private final CustomerRepository customerRepository;
    private final WarehouseReceiptRepository warehouseReceiptRepository;

    @Autowired
    public ReportController(ReportRepository repository, ReportService reportService, ReportMapper mapper, YearRepository yearRepository, CustomerRepository customerRepository, WarehouseReceiptRepository warehouseReceiptRepository) {
        this.repository = repository;
        this.reportService = reportService;
        this.mapper = mapper;
        this.yearRepository = yearRepository;
        this.customerRepository = customerRepository;
        this.warehouseReceiptRepository = warehouseReceiptRepository;
    }

    private static final Logger logger = LoggerFactory.getLogger(ReportController.class);

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
    public ResponseEntity<ReportDtoByQuery> findById(@PathVariable("id") Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(reportService.getReportById(id));
    }

    @PostMapping(path = {"/", ""})
    @Transactional
    public ResponseEntity<?> createReport(@RequestBody ReportDto reportDto) {
        Optional<Year> optionalYear = yearRepository.findByYearName(reportDto.getYearName());
        if (optionalYear.isEmpty()) {
            throw new EntityNotFoundException("سال با شناسه " + reportDto.getYearName() + " یافت نشد.");
        }
        try {
            Report report = mapper.toEntity(reportDto);
            report.setYear(optionalYear.get());
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
    public ResponseEntity<?> updateInvoice(@PathVariable Long id, @RequestBody ReportDto reportDto) {
        try {
            reportService.updateReport(id, reportDto);
            return ResponseEntity.status(HttpStatus.OK).body("فاکتور با موفقیت بروز رسانی شد.");
        } catch (PersistenceException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("خطا در به‌روزرسانی: " + e.getMessage());
        } catch (InvoiceExistByNumberAndYearNameException | InvoiceExistByNumberAndIssuedDateException |
                 InvoiceItemExistByWareHouseReceiptException | IllegalArgumentException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("خطا در به‌روزرسانی: " + e.getMessage());
        } catch (WarehouseReceiptAlreadyAssociatedException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity<?> deleteReport(@PathVariable("id") Long id) {
        try {
            reportService.deleteReport(id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("گزارش با موفقیت حذف شد.");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("گزارش با شناسه " + id + "یافت نشد.");
        } catch (DatabaseIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("خطای سمت سرور...");
        }
    }

    @PostMapping("/import")
    public ResponseEntity<String> importReports(@RequestParam("file") MultipartFile file) {
        try {
            logger.info("شروع بارگذاری ...");
            long startTime = System.currentTimeMillis();
            reportService.importReportsFromExcel(file);
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            logger.info("فایل با موفقیت باگذاری شد.");
            logger.info("پردازش آپلود فایل به مدت {} میلی‌ثانیه کامل شد", duration);
            logger.info("فایل با موفقیت باگذاری شد.");
            return ResponseEntity.status(HttpStatus.CREATED).body("فایل با موفقیت باگذاری شد.");
        } catch (RowImportException e) {
            logger.error("خطا در بارگذاری فایل:", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IOException e) {
            logger.error("خطا در بارگذاری فایل:", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("خطا در بارگذاری فایل: " + e.getMessage());
        } catch (EntityNotFoundException e) {
            logger.error("خطا در بارگذاری فایل:", e);
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
