package com.armaninvestment.parsparandreporter.controllers;

import com.armaninvestment.parsparandreporter.dtos.ReturnedByQuery;
import com.armaninvestment.parsparandreporter.dtos.ReturnedDto;
import com.armaninvestment.parsparandreporter.exceptions.DatabaseIntegrityViolationException;
import com.armaninvestment.parsparandreporter.services.ReturnedService;
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
@RequestMapping(path = "/api/returns")
public class ReturnedController {
    private final ReturnedService returnedService;

    @Autowired
    public ReturnedController(ReturnedService returnedService) {
        this.returnedService = returnedService;

    }

    @PostMapping(path = {"/", ""})
    public ResponseEntity<?> createReturned(@RequestBody ReturnedDto returnedDto) {
        try {
            returnedService.createReturned(returnedDto);
            return ResponseEntity.status(HttpStatus.CREATED).body("سند برگشت از فروش با موفقیت ایجاد شد.");
        } catch (PersistenceException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("خطا در ایجاد: " + e.getMessage());
        }
    }


    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            returnedService.importAdjustmentsFromExcel(file);
            return ResponseEntity.status(HttpStatus.CREATED).body("فایل با موفقیت باگذاری شد.");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("خطا در بارگذاری فایل: " + e.getMessage());
        }
    }


    @GetMapping(path = {"/", ""})
    public ResponseEntity<List<ReturnedByQuery>> getAllReturns() {
        List<ReturnedByQuery> list = returnedService.getAllReturned();
        return ResponseEntity.ok(list);
    }


    @GetMapping(path = "/{returnedId}")
    public ResponseEntity<ReturnedDto> getAdjustmentById(@PathVariable("returnedId") Long returnedId) {
        ReturnedDto returnedDto = returnedService.getReturnedById(returnedId);
        return ResponseEntity.status(HttpStatus.OK).body(returnedDto);
    }


    @PutMapping(path = "/{returnedId}")
    public ResponseEntity<String> updateAdjustment(@PathVariable("returnedId") Long returnedId, @RequestBody ReturnedDto returnedDto) {
        try {
            Integer result = returnedService.updateReturned(returnedId, returnedDto);
            System.out.println("result = " + result);
            return ResponseEntity.status(HttpStatus.OK).body("بروز رسانی با موفقیت انجام شد.");
        } catch (EntityNotFoundException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("خطا در بروز رسانی...");
        }
    }


    @DeleteMapping(path = "/{returnedId}")
    public ResponseEntity<?> deleteAdjustment(@PathVariable("returnedId") Long returnedId) {
        try {
            returnedService.deleteReturned(returnedId);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("سند برگشت با موفقیت حذف شد.");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("سند برگشت با شناسه " + returnedId + "یافت نشد.");
        } catch (DatabaseIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("خطای سمت سرور...");
        }
    }
}
