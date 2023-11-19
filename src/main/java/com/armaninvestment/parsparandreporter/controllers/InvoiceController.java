package com.armaninvestment.parsparandreporter.controllers;

import com.armaninvestment.parsparandreporter.dtos.ContractAndInvoiceTotalsDTO;
import com.armaninvestment.parsparandreporter.dtos.InvoiceDto;
import com.armaninvestment.parsparandreporter.dtos.InvoiceListDto;
import com.armaninvestment.parsparandreporter.exceptions.InvoiceExistByNumberAndIssuedDateException;
import com.armaninvestment.parsparandreporter.exceptions.InvoiceExistByNumberAndYearNameException;
import com.armaninvestment.parsparandreporter.exceptions.InvoiceItemExistByWareHouseReceiptException;
import com.armaninvestment.parsparandreporter.exceptions.WarehouseReceiptAlreadyAssociatedException;
import com.armaninvestment.parsparandreporter.services.InvoiceService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.List;
import java.util.Optional;

@CrossOrigin
@RestController
@RequestMapping(path = "/api/invoices")
public class InvoiceController {
    private final InvoiceService invoiceService;


    @Autowired
    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @GetMapping(path = "/max-invoice-number")
    public Long getMaxInvoiceNumber() {
        return invoiceService.getMaxInvoiceNumber();
    }

    @PostMapping(path = {"/", ""})
    public ResponseEntity<?> createInvoice(@RequestBody InvoiceDto invoiceDto) {
        try {
            InvoiceDto createdInvoice = invoiceService.createInvoice(invoiceDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdInvoice);
        } catch (PersistenceException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("خطا در ایجاد: " + e.getMessage());
        } catch (InvoiceExistByNumberAndYearNameException | InvoiceExistByNumberAndIssuedDateException |
                 InvoiceItemExistByWareHouseReceiptException | IllegalArgumentException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("خطا در ایجاد: " + e.getMessage());
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("حواله نمی تواند با بیش از یک آیتم در ارتباط باشد.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }


    @GetMapping(path = "/{id}")
    public ResponseEntity<InvoiceDto> getInvoiceById(@PathVariable("id") Long id) {
        Optional<InvoiceDto> invoiceOptional = invoiceService.getInvoiceById(id);
        return invoiceOptional.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping(path = "/contract-details/{contractId}/{invoiceId}")
    public ResponseEntity<ContractAndInvoiceTotalsDTO> getContractDetailsByContractIdAndInvoiceId(@PathVariable("contractId") Long contractId, @PathVariable("invoiceId") Long invoiceId) {
        ContractAndInvoiceTotalsDTO contractDetailsByContractIdAndInvoiceId = invoiceService.getContractDetailsByContractIdAndInvoiceId(contractId, invoiceId);
        return ResponseEntity.ok(contractDetailsByContractIdAndInvoiceId);
    }

    @GetMapping(path = {"/", ""})
    public ResponseEntity<List<InvoiceListDto>> findAll() {
        return ResponseEntity.ok(invoiceService.findAllInvoices());
    }

    @GetMapping(path = "/list/{yearName}")
    public ResponseEntity<List<InvoiceListDto>> getWarehouseReceiptList(@PathVariable("yearName") Long yearId) {
        List<InvoiceListDto> invoicesByYearName = invoiceService.findAllInvoicesByYearName(yearId);
        return new ResponseEntity<>(invoicesByYearName, HttpStatus.OK);
    }

    @GetMapping(path = "/{contractId}/findInvoiceByContract")
    public ResponseEntity<List<InvoiceListDto>> findInvoiceByContract(@PathVariable("contractId") Long contractId) {
        return ResponseEntity.ok(invoiceService.findAllInvoicesByContractId(contractId));
    }

    @PutMapping(path = "/{id}")
    public ResponseEntity<?> updateInvoice(@PathVariable Long id, @RequestBody InvoiceDto updatedInvoiceDto) {
        try {
            InvoiceDto updatedInvoice = invoiceService.updateInvoice(id, updatedInvoiceDto);
            return updatedInvoice != null ? ResponseEntity.ok(updatedInvoice) : ResponseEntity.notFound().build();
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
    public ResponseEntity<String> deleteInvoice(@PathVariable Long id) {
        try {
            invoiceService.deleteInvoice(id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("فاکتور با موفقیت حذف شد.");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("فاکتور با شناسه " + id + "یافت نشد.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("خطای سمت سرور...");
        } catch (InterruptedIOException e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping(path = "/upload")
    public ResponseEntity<String> importInvoices(@RequestParam("file") MultipartFile file) {
        try {
            invoiceService.importInvoicesFromExcel(file);
            return ResponseEntity.ok("فایل با موفقیت بارگذاری شد."); // Return a success message
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("خطا در بارگذاری فایل: " + e.getMessage()); // Return an error message
        }
    }
}
