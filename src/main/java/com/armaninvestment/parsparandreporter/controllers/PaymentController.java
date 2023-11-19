package com.armaninvestment.parsparandreporter.controllers;


import com.armaninvestment.parsparandreporter.mappers.PaymentMapper;
import com.armaninvestment.parsparandreporter.services.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@CrossOrigin
@RestController
@RequestMapping(path = "/api/payments")
public class PaymentController {
    private final PaymentService paymentService;
    public final PaymentMapper mapper;

    @Autowired
    public PaymentController(PaymentService paymentService, PaymentMapper mapper) {
        this.paymentService = paymentService;
        this.mapper = mapper;
    }

    @PostMapping("/import")
    public ResponseEntity<String> importPayments(@RequestParam("file") MultipartFile file) {
        try {
            paymentService.importPaymentsFromExcel(file);
            return ResponseEntity.status(HttpStatus.CREATED).body("فایل با موفقیت بارگذاری شد.");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("خطا در باگذاری فایل: " + e.getMessage());
        }
    }

}
