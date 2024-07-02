package com.armaninvestment.parsparandreporter.controllers;


import com.armaninvestment.parsparandreporter.dtos.PaymentDto;
import com.armaninvestment.parsparandreporter.dtos.list.PaymentDTO;
import com.armaninvestment.parsparandreporter.mappers.PaymentMapper;
import com.armaninvestment.parsparandreporter.services.PaymentService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

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

    @GetMapping("/by-customer/{customerId}")
    public ResponseEntity<List<PaymentDTO>> getPaymentsByCustomerId(@PathVariable("customerId") Long customerId) {
        List<PaymentDTO> payments = paymentService.getPaymentsByCustomerId(customerId);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentDto> findPaymentById(@PathVariable("paymentId") Long paymentId) {
        PaymentDto paymentDto = paymentService.findById(paymentId);
        return ResponseEntity.ok(paymentDto);
    }

    @PostMapping(path = "/create")
    public ResponseEntity<PaymentDto> createPayment(@RequestBody PaymentDto paymentDto) {
        PaymentDto createdPayment = paymentService.createPayment(paymentDto);
        return new ResponseEntity<>(createdPayment, HttpStatus.CREATED);
    }

    @PutMapping(path = "/update")
    public ResponseEntity<String> updatePayment(@RequestBody PaymentDto paymentDto) {
        try {
            paymentService.updatePayment(paymentDto);
            return new ResponseEntity<>("payment updated successfully", HttpStatus.OK);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("internal server error...");
        }
    }

    @DeleteMapping(path = "/{paymentId}")
    public ResponseEntity<String> deletePayment(@PathVariable("paymentId") Long paymentId) {
        try {
            paymentService.deletePayment(paymentId);
            return new ResponseEntity<>("Payment deleted successfully", HttpStatus.NO_CONTENT);
        } catch (EntityNotFoundException e) {
            e.printStackTrace();
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(path = "/import")
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
