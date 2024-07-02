package com.armaninvestment.parsparandreporter.controllers;


import com.armaninvestment.parsparandreporter.dtos.ContractDto;
import com.armaninvestment.parsparandreporter.dtos.ContractListDto;
import com.armaninvestment.parsparandreporter.dtos.ContractSummaryDto;
import com.armaninvestment.parsparandreporter.dtos.InvoicesByContractIdDto;
import com.armaninvestment.parsparandreporter.entities.Year;
import com.armaninvestment.parsparandreporter.exceptions.DatabaseIntegrityViolationException;
import com.armaninvestment.parsparandreporter.exceptions.DuplicateContractNumberException;
import com.armaninvestment.parsparandreporter.exceptions.RowImportException;
import com.armaninvestment.parsparandreporter.repositories.YearRepository;
import com.armaninvestment.parsparandreporter.services.ContractService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceException;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@CrossOrigin
@Controller
@RequestMapping(path = "/api/contracts")
public class ContractController {

    private final ContractService contractService;
    private final YearRepository yearRepository;

    @Autowired
    public ContractController(ContractService contractService,
                              YearRepository yearRepository) {
        this.contractService = contractService;
        this.yearRepository = yearRepository;
    }

    @GetMapping(path = "/{contractNumber}/find-desc-by-number")
    public ResponseEntity<String> findContractDescriptionByContractNumber(@PathVariable("contractNumber") String contractNumber) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);  // Set content type
        headers.set("X-Description-Source", "Contract-Service");

        String contractDescription = contractService.findContractDescriptionByContractNumber(contractNumber);

        return ResponseEntity.status(HttpStatus.OK)
                .headers(headers)
                .body(contractDescription);
    }


    @GetMapping("/search")
    public ResponseEntity<List<ContractListDto>> searchContractsForDropdown(
            @RequestParam("searchQuery") String searchQuery,
            @RequestParam(name = "customerId", required = false) Long customerId) {
        List<ContractListDto> matchingStatuses = contractService.searchContractsForDropdown(searchQuery, customerId);
        return ResponseEntity.ok(matchingStatuses);
    }

    @GetMapping("/search/{contractId}")
    public ResponseEntity<ContractListDto> getContractListById(@PathVariable("contractId") Long contractId) {
        ContractListDto matchingStatuses = contractService.getContractListById(contractId);
        return ResponseEntity.ok(matchingStatuses);
    }

    @GetMapping(path = {"/", ""})
    public ResponseEntity<List<ContractListDto>> getAllContracts(@RequestParam(name = "customerId", required = false) Long customerId) {
        List<ContractListDto> contractDtoList = contractService.getAllContracts(customerId);
        return ResponseEntity.ok(contractDtoList);
    }

    @GetMapping(path = "/summary")
    public ResponseEntity<List<ContractSummaryDto>> getAllContractSummaries() {
        List<ContractSummaryDto> contractSummaryDtos = contractService.getAllContractSummaries();
        return ResponseEntity.ok(contractSummaryDtos);
    }

    @PostMapping(path = {"/", ""})
    public ResponseEntity<?> create(@RequestBody ContractDto contractDto) {
        try {
            ContractDto savedContractDto = contractService.create(contractDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedContractDto);
        } catch (PersistenceException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("خطا در ایجاد: " + e.getMessage());
        } catch (DuplicateContractNumberException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("خطا در ایجاد: " + e.getMessage());
        }
    }

    @GetMapping(path = "/list/{yearName}/by-year")
    public ResponseEntity<List<ContractListDto>> findAllByYearName(@PathVariable("yearName") Long yearName) {
        Optional<Year> optionalYear = yearRepository.findByYearName(yearName);
        if (optionalYear.isPresent()) {
            List<ContractListDto> allByYearName = contractService.findAllByYearName(optionalYear.get().getName());
            return new ResponseEntity<>(allByYearName, HttpStatus.OK);
        }

        return null;
    }


    @GetMapping(path = "/{id}")
    public ResponseEntity<ContractDto> findById(@PathVariable("id") Long id) {
        ContractDto contractDto = contractService.getContractById(id);
        if (contractDto != null) {
            return new ResponseEntity<>(contractDto, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping(path = "/{contractId}/invoices_by_contract_id")
    public ResponseEntity<List<InvoicesByContractIdDto>> getAllInvoicesByContractId(@PathVariable("contractId") Long contractId) {
        List<InvoicesByContractIdDto> list = contractService.getAllInvoicesByContractId(contractId);
        if (list != null) {
            return new ResponseEntity<>(list, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping(path = "/{id}")
    public ResponseEntity<String> update(@PathVariable("id") Long id, @RequestBody ContractDto contractDto) {
        try {
            contractService.updateContract(id, contractDto);
            return ResponseEntity.status(HttpStatus.OK).body("قرارداد با موفقیت بروز رسانی شد.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/{contractId}")
    public ResponseEntity<?> deleteContract(@PathVariable Long contractId) {
        try {
            contractService.deleteContract(contractId);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("قرارداد با موفقیت حذف شد.");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("قرارداد با شناسه " + contractId + "یافت نشد.");
        } catch (DatabaseIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("خطای سمت سرور...");
        }
    }

    @PostMapping("/import")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            contractService.importContracts(file);
            return ResponseEntity.status(HttpStatus.CREATED).body("فایل با موفقیت باگذاری شد.");
        } catch (RowImportException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("خطا در بارگذاری فایل: " + e.getMessage());
        }
    }

    @GetMapping("/export-excel")
    public ResponseEntity<ByteArrayResource> exportContractsToExcel() throws IOException {
        XSSFWorkbook contractWorkbook = contractService.generateContractListExcel();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        contractWorkbook.write(outputStream);
        byte[] bytes = outputStream.toByteArray();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "contracts.xlsx");

        return new ResponseEntity<>(new ByteArrayResource(bytes), headers, HttpStatus.OK);
    }

}


