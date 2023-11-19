package com.armaninvestment.parsparandreporter.controllers;

import com.armaninvestment.parsparandreporter.services.InvoiceItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/invoice-items")
public class InvoiceItemController {


    private final InvoiceItemService invoiceItemService;

    @Autowired
    public InvoiceItemController(InvoiceItemService invoiceItemService) {
        this.invoiceItemService = invoiceItemService;
    }


    @PostMapping("/import")
    public ResponseEntity<String> importInvoiceItems(@RequestParam("file") MultipartFile file) {
        try {
            invoiceItemService.importInvoiceItemsFromExcel(file);
            return ResponseEntity.status(HttpStatus.CREATED).body("فایل با موفقیت باگذاری شد.");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("خطا در باگذاری فایل: " + e.getMessage());
        }
    }
}

