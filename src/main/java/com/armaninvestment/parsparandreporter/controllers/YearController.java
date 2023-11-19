package com.armaninvestment.parsparandreporter.controllers;

import com.armaninvestment.parsparandreporter.dtos.YearDto;
import com.armaninvestment.parsparandreporter.exceptions.DatabaseIntegrityViolationException;
import com.armaninvestment.parsparandreporter.services.YearService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@CrossOrigin
@RestController
@RequestMapping(path = "/api/years")
public class YearController {

    private final YearService yearService;

    @Autowired
    public YearController(YearService yearService) {
        this.yearService = yearService;
    }

    @PostMapping(path = {"/", ""})
    public ResponseEntity<YearDto> createYear(@RequestBody YearDto yearDto) {
        YearDto createdYear = yearService.createYear(yearDto);
        return new ResponseEntity<>(createdYear, HttpStatus.CREATED);
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<YearDto> getYear(@PathVariable Long id) {
        YearDto year = yearService.getYearById(id);
        if (year != null) {
            return ResponseEntity.ok(year);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping(path = {"/", ""})
    public ResponseEntity<List<YearDto>> getAllYears() {
        List<YearDto> years = yearService.getAllYears();
        return ResponseEntity.ok(years);
    }

    @PutMapping(path = "/{id}")
    public ResponseEntity<YearDto> updateYear(@PathVariable Long id, @RequestBody YearDto yearDto) {
        YearDto updatedYear = yearService.updateYear(id, yearDto);
        if (updatedYear != null) {
            return ResponseEntity.ok(updatedYear);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{yearId}")
    public ResponseEntity<?> deleteYear(@PathVariable("yearId") Long yearId) {
        try {
            yearService.deleteYear(yearId);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("سال با موفقیت حذف شد.");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("سال با شناسه " + yearId + "یافت نشد.");
        } catch (DatabaseIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("خطای سمت سرور...");
        }
    }
}
