package com.armaninvestment.parsparandreporter.controllers;


import com.armaninvestment.parsparandreporter.dtos.NotInvoiced;
import com.armaninvestment.parsparandreporter.dtos.WarehouseReceiptDto;
import com.armaninvestment.parsparandreporter.dtos.list.WarehouseReceiptListDto;
import com.armaninvestment.parsparandreporter.exceptions.DatabaseIntegrityViolationException;
import com.armaninvestment.parsparandreporter.exceptions.WareHouseReceiptRepetitiveByNumberAndDateException;
import com.armaninvestment.parsparandreporter.exceptions.WareHouseReceiptRepetitiveByNumberAndYearException;
import com.armaninvestment.parsparandreporter.services.WarehouseReceiptService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceException;
import jakarta.transaction.Transactional;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@CrossOrigin
@RestController
@RequestMapping(path = "/api/warehouse-receipts")
public class WarehouseReceiptController {
    private final WarehouseReceiptService warehouseReceiptService;

    @Autowired
    public WarehouseReceiptController(WarehouseReceiptService warehouseReceiptService) {
        this.warehouseReceiptService = warehouseReceiptService;
    }

    @GetMapping(path = {"/not-invoiced/", "/not-invoiced"})
    public ResponseEntity<List<NotInvoiced>> findNotInvoicedByYearAndCustomer(@RequestParam(value = "customerCode", required = false) String customerCode,
                                                                              @RequestParam(value = "yearName", required = false) Long yearName,
                                                                              @RequestParam(value = "invoiced", required = false) Boolean invoiced
    ) {
        List<NotInvoiced> notInvoicelist = warehouseReceiptService.findNotInvoicedByYearAndCustomer(customerCode, yearName, invoiced);
        return ResponseEntity.ok(notInvoicelist);
    }

    @GetMapping(path = {"/", ""})
    public ResponseEntity<List<WarehouseReceiptDto>> getAllWarehouseReceipts() {
        List<WarehouseReceiptDto> warehouseReceipts = warehouseReceiptService.getAllWarehouseReceipts();
        return new ResponseEntity<>(warehouseReceipts, HttpStatus.OK);
    }

    @GetMapping(path = "/list/{yearId}")
    public ResponseEntity<List<WarehouseReceiptListDto>> getWarehouseReceiptList(@PathVariable("yearId") Long yearId) {
        List<WarehouseReceiptListDto> warehouseReceiptList = warehouseReceiptService.getWarehouseReceiptList(yearId);
        return new ResponseEntity<>(warehouseReceiptList, HttpStatus.OK);
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<WarehouseReceiptDto> getWarehouseReceiptById(@PathVariable Long id) {
        Optional<WarehouseReceiptDto> warehouseReceipt = warehouseReceiptService.getWarehouseReceiptById(id);
        return warehouseReceipt.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping(path = {"/", ""})
    @Transactional
    public ResponseEntity<?> createWarehouseReceipt(@RequestBody WarehouseReceiptDto warehouseReceiptDto) {
        try {
            WarehouseReceiptDto createdWarehouseReceipt = warehouseReceiptService.createWarehouseReceipt(warehouseReceiptDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdWarehouseReceipt);
        } catch (WareHouseReceiptRepetitiveByNumberAndDateException |
                 WareHouseReceiptRepetitiveByNumberAndYearException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("خطا در ایجاد: " + e.getMessage());
        } catch (PersistenceException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("خطا در ایجاد: " + e.getMessage());
        }
    }

    @PutMapping(path = "/{id}")
    public ResponseEntity<WarehouseReceiptDto> updateWarehouseReceipt(@PathVariable Long id, @RequestBody WarehouseReceiptDto warehouseReceiptDto) {
        Optional<WarehouseReceiptDto> updatedWarehouseReceipt = warehouseReceiptService.updateWarehouseReceipt(id, warehouseReceiptDto);
        return updatedWarehouseReceipt.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity<String> deleteWarehouseReceipt(@PathVariable Long id) {
        try {
            warehouseReceiptService.deleteWarehouseReceipt(id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("حواله با موفقیت حذف شد.");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("حواله با شناسه " + id + "یافت نشد.");
        } catch (DatabaseIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("خطای سمت سرور...");
        }
    }

    @PostMapping(path = "/import")
    public ResponseEntity<String> importWarehouseReceipts(@RequestParam("file") MultipartFile file) {
        try {
            warehouseReceiptService.importWarehouseReceiptsFromExcel(file);
            return ResponseEntity.status(HttpStatus.CREATED).body("فایل با موفقیت باگذاری شد.");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("خطا در باگذاری فایل: " + e.getMessage());
        }
    }

    @GetMapping(path = "/export-excel")
    public ResponseEntity<ByteArrayResource> exportWarehouseReceiptsToExcel() throws IOException {
        XSSFWorkbook warehouseReceiptWorkbook = warehouseReceiptService.generateWarehouseReceiptListExcel();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        warehouseReceiptWorkbook.write(outputStream);
        byte[] bytes = outputStream.toByteArray();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "warehouse-receipts.xlsx");

        return new ResponseEntity<>(new ByteArrayResource(bytes), headers, HttpStatus.OK);
    }
}

