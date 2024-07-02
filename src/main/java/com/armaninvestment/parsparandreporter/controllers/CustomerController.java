package com.armaninvestment.parsparandreporter.controllers;

import com.armaninvestment.parsparandreporter.dtos.*;
import com.armaninvestment.parsparandreporter.entities.CustomerDtoByQuery;
import com.armaninvestment.parsparandreporter.exceptions.DatabaseIntegrityViolationException;
import com.armaninvestment.parsparandreporter.exceptions.RowImportException;
import com.armaninvestment.parsparandreporter.services.CustomerService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceException;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping(path = "/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    @Autowired
    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping(path = {"/not-invoiced/{customerId}", "/not-invoiced/{customerId}/"})
    @ResponseBody
    public List<NotInvoicedReceipt> findNotInvoicedReceiptsByCustomerId(@PathVariable Long customerId) {
        return customerService.findNotInvoicedReceiptsByCustomerId(customerId);
    }

    @GetMapping(path = "/invoice-upload-by-customerId/{id}")
    public ResponseEntity<ByteArrayResource> getInvoicesForCustomer(@PathVariable("id") Long customerId) throws IOException {

        XSSFWorkbook workbook = customerService.generateInvoiceUploadExcel(customerId);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        byte[] bytes = outputStream.toByteArray();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "invoice-upload.xlsx");

        return new ResponseEntity<>(new ByteArrayResource(bytes), headers, HttpStatus.OK);
    }

    @GetMapping("/{customerId}/name")
    public ResponseEntity<String> getCustomerNameById(@PathVariable Long customerId) {
        try {
            String customerName = customerService.getCustomerNameById(customerId);
            if (customerName != null) {
                return new ResponseEntity<>(customerName, HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Customer not found", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{customerId}/{yearName}/not-invoiced-details")
    public List<NotInvoicedDetailsDto> getNotInvoicedAmountDetailsByCustomerIdAndYearName(
            @PathVariable Long customerId, @PathVariable Long yearName) {
        return customerService.getNotInvoicedAmountDetailsByCustomerIdAndYearName(customerId, yearName);
    }

    @GetMapping("/{customerId}/summary")
    public ClientSummaryResult getClientSummary(@PathVariable Long customerId) {
        return customerService.getClientSummaryByCustomerId(customerId);
    }

    @GetMapping("/{customerId}/{contractNumber}/summary-details")
    public ResponseEntity<List<ClientSummaryDetailsDTO>> getClientSummaryDetails(
            @PathVariable Long customerId,
            @PathVariable String contractNumber) {

        List<ClientSummaryDetailsDTO> clientSummaryDetails = customerService.getClientSummaryDetailsByCustomerId(customerId, contractNumber);

        return ResponseEntity.ok(clientSummaryDetails);
    }

    @GetMapping("/search")
    public ResponseEntity<List<CustomerSelectDto>> searchCustomersForDropdown(@RequestParam("searchQuery") String searchQuery) {
        List<CustomerSelectDto> matchingCustomers = customerService.searchCustomersForDropdown(searchQuery);
        return ResponseEntity.ok(matchingCustomers);
    }

    @GetMapping("/invoice-summary/{customerId}/{yearName}")
    public ResponseEntity<List<MonthlyCustomerInvoiceSummaryDTO>> getMonthlySummary(
            @PathVariable Long customerId,
            @PathVariable Long yearName) {
        List<MonthlyCustomerInvoiceSummaryDTO> result = customerService.getCustomerInvoiceMonthlySummary(customerId, yearName);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping(path = {"/", ""})
    public ResponseEntity<List<CustomerSelectDto>> findAll() {
        List<CustomerSelectDto> customers = customerService.getAllCustomersForDropdown();
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/{year}/monthly-sales")
    public ResponseEntity<List<MonthlySales>> findByPersianYearAndGroupedByMonth(@PathVariable("year") Short year) {
        List<MonthlySales> monthlySalesList = customerService.getMonthlySalesByPersianYear(year);
        return ResponseEntity.ok(monthlySalesList);
    }

    @GetMapping("/{customerId}/{year}/customer-totals")
    public ResponseEntity<CustomerTotalsDto> calculateCustomerTotals(@PathVariable("customerId") Long customerId, @PathVariable("year") Long year) {
        return ResponseEntity.status(HttpStatus.OK).body(customerService.calculateCustomerTotals(customerId, year));
    }

    @GetMapping("/{year}/monthly-payments")
    public ResponseEntity<List<MonthlyPayment>> findPaymentsByPersianYearAndGroupedByMonth(@PathVariable("year") Short year) {
        List<MonthlyPayment> monthlyPayments = customerService.getMonthlyPaymentsByPersianYear(year);
        return ResponseEntity.ok(monthlyPayments);
    }

    @GetMapping("/{customerId}/{year}/monthly-sales")
    public ResponseEntity<List<MonthlySales>> findByCustomerAndPersianYearAndGroupedByMonth(
            @PathVariable BigInteger customerId, @PathVariable("year") Short year) {
        List<MonthlySales> monthlySalesList = customerService.getMonthlySalesByCustomerAndPersianYear(customerId, year);
        return ResponseEntity.ok(monthlySalesList);
    }

    @GetMapping("/{customerId}/{year}/monthly-payments")
    public ResponseEntity<List<MonthlyPayment>> findPaymentsByCustomerAndPersianYearAndGroupedByMonth(
            @PathVariable BigInteger customerId, @PathVariable("year") Short year) {
        List<MonthlyPayment> monthlyPayments = customerService.getMonthlyPaymentsByCustomerAndPersianYear(customerId, year);
        return ResponseEntity.ok(monthlyPayments);
    }

    @GetMapping("/{customerId}/{year}/monthly_report")
    public ResponseEntity<List<MonthlyReport>> getCustomerSummary(
            @PathVariable Integer customerId, @PathVariable Integer year) {
        List<MonthlyReport> reportList = customerService.getCustomerSummary(customerId, year);
        return ResponseEntity.ok(reportList);
    }

    @GetMapping(path = "/{customerCode}/{year}/customer_report")
    public ResponseEntity<List<CustomerReport>> getCustomerReportsByYearAndCustomerId(
            @PathVariable String customerCode, @PathVariable Long year) {
        List<CustomerReport> reportList = customerService.getCustomerReportsByYearAndCustomerId(customerCode, year);
        return ResponseEntity.ok(reportList);
    }

    @GetMapping(path = "/{customerCode}/{year}/customer_payment")
    public ResponseEntity<List<CustomerPaymentDto>> getPaymentsByCustomerCodeAndYearName(
            @PathVariable String customerCode, @PathVariable Long year) {
        List<CustomerPaymentDto> reportList = customerService.getPaymentsByCustomerCodeAndYearName(customerCode, year);
        return ResponseEntity.ok(reportList);
    }

    @GetMapping(path = "/{customerCode}/{year}/customer_invoice")
    public ResponseEntity<List<CustomerInvoiceDto>> getCustomerInvoicesByYearAndCustomerCode(
            @PathVariable String customerCode, @PathVariable Long year) {
        List<CustomerInvoiceDto> reportList = customerService.getCustomerInvoicesByYearAndCustomerCode(customerCode, year);
        return ResponseEntity.ok(reportList);
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<?> findById(@PathVariable("id") Long id) {
        try {
            CustomerDtoByQuery customerById = customerService.findCustomerById(id);
            return ResponseEntity.status(HttpStatus.OK).body(customerById);
        } catch (EntityNotFoundException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping(path = {"/", ""})
    public ResponseEntity<?> createCustomer(@RequestBody CustomerDto customerDto) {
        try {
            CustomerDto createdCustomerDto = customerService.createCustomer(customerDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdCustomerDto);
        } catch (DatabaseIntegrityViolationException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("خطا در ایجاد: " + e.getMessage());
        } catch (PersistenceException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("خطا در ایجاد: " + e.getMessage());
        }
    }


    @PutMapping(path = "/{id}")
    public ResponseEntity<CustomerDto> updateCustomer(@PathVariable("id") Long id, @RequestBody CustomerDto customerDto) {
        CustomerDto updatedCustomerDto = customerService.updateCustomer(id, customerDto);
        if (updatedCustomerDto != null) {
            return ResponseEntity.ok(updatedCustomerDto);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{customerId}")
    public ResponseEntity<?> deleteCustomer(@PathVariable Long customerId) {
        try {
            customerService.deleteCustomer(customerId);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("مشتری با موفقیت حذف شد.");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("مشتری با شناسه " + customerId + "یافت نشد.");
        } catch (DatabaseIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("خطای سمت سرور...");
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            customerService.importCustomersFromExcel(file);
            return ResponseEntity.status(HttpStatus.CREATED).body("فایل با موفقیت بارگذاری شد.");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("خطا در بارگذاری فایل: " + e.getMessage()); // Return an error message
        }
    }

    @PostMapping("/upload-payments")
    public ResponseEntity<String> uploadPaymentFile(@RequestParam("file") MultipartFile file) {
        try {
            customerService.importCustomerPaymentsFromExcel(file);
            return ResponseEntity.status(HttpStatus.CREATED).body("فایل با موفقیت بارگذاری شد.");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("خطا در بارگذاری فایل: " + e.getMessage());
        } catch (RowImportException e) {
            e.printStackTrace();
            if (e.getMessage().equals("Cannot get a STRING value from a NUMERIC cell")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("خطا در بارگذاری فایل: امکان خواندن متن از سلول با فرمت عددی وجود ندارد.");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("خطا در بارگذاری فایل: " + e.getMessage());
            }
        }
    }

}
