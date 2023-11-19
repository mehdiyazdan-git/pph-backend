package com.armaninvestment.parsparandreporter.controllers;


import com.armaninvestment.parsparandreporter.mappers.ReportItemMapper;
import com.armaninvestment.parsparandreporter.repositories.ReportItemRepository;
import com.armaninvestment.parsparandreporter.services.ReportItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@CrossOrigin
@RestController
@RequestMapping(path = "/api/reportItems")
public class ReportItemController {
    private final ReportItemRepository repository;

    private final ReportItemService reportItemService;
    private final ReportItemMapper mapper;

    @Autowired
    public ReportItemController(ReportItemRepository repository, ReportItemService reportItemService, ReportItemMapper mapper) {
        this.repository = repository;
        this.reportItemService = reportItemService;
        this.mapper = mapper;
    }

    @PostMapping("/import")
    public ResponseEntity<String> importReportItems(@RequestParam("file") MultipartFile file) {
        try {
            reportItemService.importReportItemsFromExcel(file);
            return ResponseEntity.ok("فایل با موفقیت باگذاری شد.");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("خطا در باگذاری فایل: " + e.getMessage());
        }
    }

}
