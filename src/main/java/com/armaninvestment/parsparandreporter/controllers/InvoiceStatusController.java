package com.armaninvestment.parsparandreporter.controllers;

import com.armaninvestment.parsparandreporter.dtos.InvoiceStatusDto;
import com.armaninvestment.parsparandreporter.exceptions.DatabaseIntegrityViolationException;
import com.armaninvestment.parsparandreporter.services.InvoiceStatusService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin
@RestController
@RequestMapping(path = "/api/invoice-status")
public class InvoiceStatusController {

    private final InvoiceStatusService invoiceStatusService;

    @Autowired
    public InvoiceStatusController(InvoiceStatusService invoiceStatusService) {
        this.invoiceStatusService = invoiceStatusService;
    }

    @GetMapping("/search")
    public ResponseEntity<List<InvoiceStatusDto>> searchCustomersForDropdown(@RequestParam("searchQuery") String searchQuery) {
        List<InvoiceStatusDto> matchingStatuses = invoiceStatusService.searchStatusesForDropdown(searchQuery);
        return ResponseEntity.ok(matchingStatuses);
    }

    @PostMapping(path = {"/", ""})
    public ResponseEntity<?> createInvoiceStatus(@RequestBody InvoiceStatusDto invoiceStatusDto) {
        try {
            InvoiceStatusDto createdInvoiceStatus = invoiceStatusService.createInvoiceStatus(invoiceStatusDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdInvoiceStatus);
        } catch (PersistenceException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("خطا در ایجاد: " + e.getMessage());
        }
    }


    @GetMapping(path = {"/", ""})
    public ResponseEntity<List<InvoiceStatusDto>> getAllInvoiceStatuses() {
        List<InvoiceStatusDto> invoiceStatusList = invoiceStatusService.getAllInvoiceStatuses();
        return new ResponseEntity<>(invoiceStatusList, HttpStatus.OK);
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<InvoiceStatusDto> getInvoiceStatusById(@PathVariable Integer id) {
        Optional<InvoiceStatusDto> invoiceStatus = invoiceStatusService.getInvoiceStatusById(id);
        return invoiceStatus.map(status -> new ResponseEntity<>(status, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PutMapping(path = "/{id}")
    public ResponseEntity<InvoiceStatusDto> updateInvoiceStatus(@RequestBody InvoiceStatusDto invoiceStatusDto) {
        try {
            InvoiceStatusDto updatedInvoiceStatus = invoiceStatusService.updateInvoiceStatus(invoiceStatusDto);
            return new ResponseEntity<>(updatedInvoiceStatus, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity<?> deleteInvoiceStatus(@PathVariable Integer id) {
        try {
            invoiceStatusService.deleteInvoiceStatus(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (DatabaseIntegrityViolationException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("امکان حذف وضعیت وجود ندارد چون فاکتور های مرتبط دارد.");
        }
    }
}
