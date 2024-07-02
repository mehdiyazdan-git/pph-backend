package com.armaninvestment.parsparandreporter.controllers;

import com.armaninvestment.parsparandreporter.dtos.AdjustmentByQuery;
import com.armaninvestment.parsparandreporter.dtos.AdjustmentDto;
import com.armaninvestment.parsparandreporter.exceptions.DatabaseIntegrityViolationException;
import com.armaninvestment.parsparandreporter.services.AdjustmentService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping(path = "/api/adjustments")
public class AdjustmentController {
    private final AdjustmentService adjustmentService;

    @Autowired
    public AdjustmentController(AdjustmentService productService) {
        this.adjustmentService = productService;

    }

    @PostMapping(path = {"/", ""})
    public ResponseEntity<?> createProduct(@RequestBody AdjustmentDto adjustmentDto) {
        try {
            adjustmentService.createAdjustment(adjustmentDto);
            return ResponseEntity.status(HttpStatus.CREATED).body("سند تعدیل با موفقیت ایجاد شد.");
        } catch (PersistenceException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("خطا در ایجاد: " + e.getMessage());
        }
    }


    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            adjustmentService.importAdjustmentsFromExcel(file);
            return ResponseEntity.status(HttpStatus.CREATED).body("فایل با موفقیت باگذاری شد.");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("خطا در بارگذاری فایل: " + e.getMessage());
        }
    }


    @GetMapping(path = {"/", ""})
    public ResponseEntity<List<AdjustmentByQuery>> getAllAdjustments() {
        List<AdjustmentByQuery> list = adjustmentService.getAllAdjustments();
        return ResponseEntity.ok(list);
    }


    @GetMapping(path = "/{adjustmentId}")
    public ResponseEntity<AdjustmentDto> getAdjustmentById(@PathVariable("adjustmentId") Long adjustmentId) {
        List<AdjustmentDto> list = adjustmentService.getAdjustmentById(adjustmentId);
        return ResponseEntity.status(HttpStatus.OK).body(list.get(0));
    }


    @PutMapping(path = "/{adjustmentId}")
    public ResponseEntity<?> updateAdjustment(@PathVariable("adjustmentId") Long adjustmentId, @RequestBody AdjustmentDto adjustmentDto) {
        try {
            adjustmentService.updateAdjustment(adjustmentId, adjustmentDto);
            return ResponseEntity.status(HttpStatus.OK).body("سند تعدیل با موفقیت بروز رسانی شد.");
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("خطا در بروز رسانی");
        }
    }


    @DeleteMapping(path = "/{adjustmentId}")
    public ResponseEntity<?> deleteAdjustment(@PathVariable("adjustmentId") Long adjustmentId) {
        try {
            adjustmentService.deleteAdjustment(adjustmentId);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("محصول با موفقیت حذف شد.");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("محصول با شناسه " + adjustmentId + "یافت نشد.");
        } catch (DatabaseIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("خطای سمت سرور...");
        }
    }
}
