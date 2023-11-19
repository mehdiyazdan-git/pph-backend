package com.armaninvestment.parsparandreporter.services;

import com.armaninvestment.parsparandreporter.dtos.*;
import com.armaninvestment.parsparandreporter.entities.Customer;
import com.armaninvestment.parsparandreporter.exceptions.DatabaseIntegrityViolationException;
import com.armaninvestment.parsparandreporter.mappers.CustomerMapper;
import com.armaninvestment.parsparandreporter.mappers.CustomerSelectMapper;
import com.armaninvestment.parsparandreporter.repositories.ContractRepository;
import com.armaninvestment.parsparandreporter.repositories.CustomerRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerSelectMapper selectMapper;
    private final CustomerMapper customerMapper;
    private final CustomerSelectMapper customerSelectMapper;
    private final ContractRepository contractRepository;

    @Autowired
    public CustomerService(CustomerRepository customerRepository, CustomerSelectMapper selectMapper, CustomerMapper customerMapper, CustomerSelectMapper customerSelectMapper,
                           ContractRepository contractRepository) {
        this.customerRepository = customerRepository;
        this.selectMapper = selectMapper;
        this.customerMapper = customerMapper;
        this.customerSelectMapper = customerSelectMapper;
        this.contractRepository = contractRepository;
    }

    public List<CustomerSelectDto> getAllCustomersForDropdown() {
        List<Customer> customers = customerRepository.findAll();
        return customers.stream()
                .map(selectMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<CustomerSelectDto> searchCustomersForDropdown(String searchQuery) {
        List<Customer> matchingCustomers = customerRepository.findByNameContaining(searchQuery);
        return matchingCustomers.stream()
                .map(customerSelectMapper::toDto)
                .toList();
    }

    public List<MonthlySales> getMonthlySalesByPersianYear(Short year) {
        List<Object[]> salesData = customerRepository.findMonthlySalesByPersianYear(year);
        return mapSalesDataToMonthlySalesList(salesData);
    }

    public List<MonthlyPayment> getMonthlyPaymentsByPersianYear(Short year) {
        List<Object[]> paymentsData = customerRepository.findMonthlyPaymentsByPersianYear(year);
        return mapPaymentsDataToMonthlyPaymentsList(paymentsData);
    }

    public List<MonthlySales> getMonthlySalesByCustomerAndPersianYear(BigInteger customerId, Short year) {
        List<Object[]> salesData = customerRepository.findMonthlySalesByCustomerAndPersianYear(customerId, year);
        return mapSalesDataToMonthlySalesList(salesData);
    }

    public List<MonthlyPayment> getMonthlyPaymentsByCustomerAndPersianYear(BigInteger customerId, Short year) {
        List<Object[]> paymentsData = customerRepository.findMonthlyPaymentsByCustomerAndPersianYear(customerId, year);
        return mapPaymentsDataToMonthlyPaymentsList(paymentsData);
    }

    public List<MonthlyReport> getCustomerSummary(Integer customerId, Integer year) {
        List<Object[]> reportData = customerRepository.getMonthlyReport(customerId, year);
        return mapReportDataToMonthlyReportList(reportData);
    }

    public List<CustomerReport> getCustomerReportsByYearAndCustomerId(String customerCode, Long yearName) {
        List<Object[]> objectList = customerRepository.getCustomerReportsByYearAndCustomerId(customerCode, yearName);
        List<CustomerReport> customerReportList = new ArrayList<>();
        for (Object[] obj : objectList) {
            CustomerReport customerReport = new CustomerReport();
            customerReport.setReportDate((String) obj[0]);
            customerReport.setReportExplanation((String) obj[1]);
            customerReport.setTotalAmount((BigDecimal) obj[2]);
            customerReport.setTotalQuantity((Long) obj[3]);
            customerReportList.add(customerReport);
        }
        return customerReportList;
    }

    public List<CustomerInvoiceDto> getCustomerInvoicesByYearAndCustomerCode(String customerCode, Long yearName) {
        List<Object[]> objectList = customerRepository.getCustomerInvoicesByYearAndCustomerCode(customerCode, yearName);
        List<CustomerInvoiceDto> customerInvoiceDtoList = new ArrayList<>();
        for (Object[] obj : objectList) {
            CustomerInvoiceDto customerInvoiceDto = new CustomerInvoiceDto();
            customerInvoiceDto.setId((Long) obj[0]);
            customerInvoiceDto.setInvoiceNumber((Long) obj[1]);
            customerInvoiceDto.setInvoiceDate((String) obj[2]);
            customerInvoiceDto.setTotalAmount((BigDecimal) obj[3]);
            customerInvoiceDto.setTotalQuantity((Long) obj[4]);
            customerInvoiceDtoList.add(customerInvoiceDto);
        }
        return customerInvoiceDtoList;
    }

    public List<CustomerPaymentDto> getPaymentsByCustomerCodeAndYearName(String customerCode, Long yearName) {
        List<Object[]> objectList = customerRepository.getPaymentsByCustomerCodeAndYearName(customerCode, yearName);
        List<CustomerPaymentDto> paymentDtoList = new ArrayList<>();
        for (Object[] obj : objectList) {
            CustomerPaymentDto paymentDto = new CustomerPaymentDto();
            paymentDto.setPaymentDate((String) obj[0]);
            paymentDto.setPaymentAmount((Double) obj[1]);
            paymentDtoList.add(paymentDto);
        }
        return paymentDtoList;
    }

    public CustomerDto findCustomerById(Long id) {
        Optional<Customer> optionalCustomer = customerRepository.findById(id);
        return optionalCustomer.map(customerMapper::toDto).orElse(null);
    }

    public CustomerDto createCustomer(CustomerDto customerDto) {
        Customer customer = customerMapper.toEntity(customerDto);
        Customer savedCustomer = customerRepository.save(customer);
        return customerMapper.toDto(savedCustomer);
    }

    public CustomerDto updateCustomer(Long id, CustomerDto customerDto) {
        Optional<Customer> optionalCustomer = customerRepository.findById(id);
        if (optionalCustomer.isPresent()) {
            Customer customer = optionalCustomer.get();
            Customer updatedCustomer = customerMapper.partialUpdate(customerDto, customer);
            Customer savedCustomer = customerRepository.save(updatedCustomer);
            return customerMapper.toDto(savedCustomer);
        } else {
            return null;
        }
    }

    public void deleteCustomer(Long id) {
        Optional<Customer> optionalCustomer = customerRepository.findById(id);
        if (optionalCustomer.isEmpty()) {
            throw new EntityNotFoundException("مشتری ای با شناسه " + id + "یافت نشد.");
        }
        Customer customer = optionalCustomer.get();

        if (!customer.getPayments().isEmpty()) {
            throw new DatabaseIntegrityViolationException("امکان حذف مشتری وجود ندارد چون پرداخت‌های مرتبط دارد.");
        }
        if (!customer.getReportItems().isEmpty()) {
            throw new DatabaseIntegrityViolationException("امکان حذف مشتری وجود ندارد چون آیتم‌های گزارش مرتبط دارد.");
        }
        if (!customer.getContracts().isEmpty()) {
            throw new DatabaseIntegrityViolationException("امکان حذف مشتری وجود ندارد چون قراردادها مرتبط دارد.");
        }
        customerRepository.deleteById(id);
    }

    private List<MonthlySales> mapSalesDataToMonthlySalesList(List<Object[]> salesData) {
        List<MonthlySales> monthlySalesList = new ArrayList<>();

        for (Object[] obj : salesData) {
            MonthlySales monthlySales = new MonthlySales();
            monthlySales.setMonth((Short) obj[0]);
            monthlySales.setMonthName((String) obj[1]);
            monthlySales.setTotalAmount((BigDecimal) obj[2]);
            monthlySales.setTotalQuantity((Long) obj[3]);
            monthlySalesList.add(monthlySales);
        }

        return monthlySalesList;
    }

    private List<MonthlyPayment> mapPaymentsDataToMonthlyPaymentsList(List<Object[]> paymentsData) {
        List<MonthlyPayment> monthlyPaymentsList = new ArrayList<>();

        for (Object[] obj : paymentsData) {
            MonthlyPayment monthlyPayment = new MonthlyPayment();
            monthlyPayment.setMonth((Short) obj[0]);
            monthlyPayment.setMonthName((String) obj[1]);
            monthlyPayment.setTotalAmount((Double) obj[2]);
            // Add any other mappings needed
            monthlyPaymentsList.add(monthlyPayment);
        }

        return monthlyPaymentsList;
    }

    private List<MonthlyReport> mapReportDataToMonthlyReportList(List<Object[]> reportData) {
        List<MonthlyReport> monthlyReportList = new ArrayList<>();

        for (Object[] obj : reportData) {
            MonthlyReport monthlyReport = new MonthlyReport();
            monthlyReport.setId((Long) obj[0]);
            monthlyReport.setName((String) obj[1]);
            monthlyReport.setTotalAmount((BigDecimal) obj[2]);
            monthlyReport.setTotalCount((Long) obj[3]);
            monthlyReport.setAvgPrice((BigDecimal) obj[4]);
            // Add any other mappings needed
            monthlyReportList.add(monthlyReport);
        }

        return monthlyReportList;
    }

    @Transactional
    public void importCustomersFromExcel(MultipartFile file) throws IOException {
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0); // Assuming the data is in the first sheet

            for (Row currentRow : sheet) {
                if (currentRow.getRowNum() == 0) {
                    // Skip the header row
                    continue;
                }

                String name = currentRow.getCell(0).getStringCellValue();
                String phone = currentRow.getCell(1).getStringCellValue();
                String customerCode = currentRow.getCell(2).getStringCellValue();
                String economicCode = currentRow.getCell(3).getStringCellValue();
                String nationalCode = currentRow.getCell(4).getStringCellValue();


                // Create and save a new Contract entity
                Customer customer = new Customer();
                customer.setName(name);
                customer.setPhone(phone);
                customer.setCustomerCode(customerCode);
                customer.setEconomicCode(economicCode);
                customer.setNationalCode(nationalCode);

                customerRepository.save(customer);
            }
        }
    }

    public XSSFWorkbook generateCustomerListExcel() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Customers");

        List<CustomerDto> customerList = customerRepository.findAll().stream().map(customerMapper::toDto).toList();

        XSSFRow headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("شناسه مشتری");
        headerRow.createCell(1).setCellValue("نام مشتری");
        headerRow.createCell(2).setCellValue("شماره تماس");
        headerRow.createCell(3).setCellValue("کد تفضیلی مشتری");
        headerRow.createCell(4).setCellValue("شماره اقتصادی");
        headerRow.createCell(5).setCellValue("کد ملی");

        int rowNum = 1;
        for (CustomerDto customer : customerList) {
            XSSFRow row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(customer.getId());
            row.createCell(1).setCellValue(customer.getName());
            row.createCell(2).setCellValue(customer.getPhone());
            row.createCell(3).setCellValue(customer.getCustomerCode());
            row.createCell(4).setCellValue(customer.getEconomicCode());
            row.createCell(5).setCellValue(customer.getNationalCode());
        }

        return workbook;
    }

}

