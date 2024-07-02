package com.armaninvestment.parsparandreporter.services;

import com.armaninvestment.parsparandreporter.dtos.*;
import com.armaninvestment.parsparandreporter.dtos.report.AdjustmentReportDto;
import com.armaninvestment.parsparandreporter.dtos.report.NotInvoicedReportDto;
import com.armaninvestment.parsparandreporter.dtos.report.PaymentReportDto;
import com.armaninvestment.parsparandreporter.entities.*;
import com.armaninvestment.parsparandreporter.exceptions.DatabaseIntegrityViolationException;
import com.armaninvestment.parsparandreporter.exceptions.RowImportException;
import com.armaninvestment.parsparandreporter.mappers.CustomerMapper;
import com.armaninvestment.parsparandreporter.mappers.CustomerSelectMapper;
import com.armaninvestment.parsparandreporter.repositories.*;
import com.github.eloyzone.jalalicalendar.DateConverter;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.Data;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerSelectMapper selectMapper;
    private final CustomerMapper customerMapper;
    private final CustomerSelectMapper customerSelectMapper;
    private final ContractRepository contractRepository;
    private final YearRepository yearRepository;
    private final InvoiceRepository invoiceRepository;

    private final WarehouseInvoiceRepository warehouseInvoiceRepository;
    private final WarehouseReceiptItemRepository warehouseReceiptItemRepository;
    private final PaymentRepository paymentRepository;
    private final WarehouseReceiptRepository warehouseReceiptRepository;
    private final EstablishmentService establishmentService;

    @Autowired
    public CustomerService(CustomerRepository customerRepository, CustomerSelectMapper selectMapper, CustomerMapper customerMapper, CustomerSelectMapper customerSelectMapper,
                           ContractRepository contractRepository,
                           YearRepository yearRepository, InvoiceRepository invoiceRepository, WarehouseInvoiceRepository warehouseInvoiceRepository, WarehouseReceiptItemRepository warehouseReceiptItemRepository,
                           PaymentRepository paymentRepository,
                           WarehouseReceiptRepository warehouseReceiptRepository, EstablishmentService establishmentService) {
        this.customerRepository = customerRepository;
        this.selectMapper = selectMapper;
        this.customerMapper = customerMapper;
        this.customerSelectMapper = customerSelectMapper;
        this.contractRepository = contractRepository;
        this.yearRepository = yearRepository;
        this.invoiceRepository = invoiceRepository;
        this.warehouseInvoiceRepository = warehouseInvoiceRepository;
        this.warehouseReceiptItemRepository = warehouseReceiptItemRepository;
        this.paymentRepository = paymentRepository;
        this.warehouseReceiptRepository = warehouseReceiptRepository;
        this.establishmentService = establishmentService;
    }

    public List<NotInvoicedReceipt> findNotInvoicedReceiptsByCustomerId(Long customerId) {
        List<Object[]> queryResults = warehouseInvoiceRepository.findNotInvoicedReceiptsByCustomerId(customerId);
        return queryResults.stream()
                .map(result -> new NotInvoicedReceipt(
                        (Long) result[0],
                        (String) result[1],
                        (String) result[2],
                        (Double) result[3],
                        (Double) result[4]))
                .collect(Collectors.toList());
    }

    public String getCustomerNameById(Long customerId) {
        return customerRepository.getCustomerNameById(customerId);
    }

    public ClientSummaryResult getClientSummaryByCustomerId(Long customerId) {
        List<Object[]> objects = warehouseReceiptItemRepository.getClientSummaryByCustomerId(customerId);
        List<ClientSummaryDTO> list = new ArrayList<>();
        for (Object[] obj : objects) {
            ClientSummaryDTO dto = new ClientSummaryDTO();
            dto.setContractNumber((String) obj[0]);
            dto.setAdvancedPayment((Double) obj[1]);
            dto.setPerformanceBound((Double) obj[2]);
            dto.setInsuranceDeposit((Double) obj[3]);
            dto.setSalesAmount((Double) obj[4]);
            dto.setSalesQuantity((Double) obj[5]);
            dto.setVat((Double) obj[6]);
            list.add(dto);
        }

        PaymentReportDto totalPaymentByCustomerId = getPaymentGroupBySubjectFilterByCustomerId(customerId);
        EstablishmentDto establishmentDto = this.getEstablishmentByCustomerId(customerId);

        Double performanceBoundCoefficient = 0d;
        Double insuranceDepositCoefficient = 0d;

        Optional<Contract> optionalContract = contractRepository.findLastContractByCustomerId(customerId);
        if (optionalContract.isPresent()) {
            Contract contract = optionalContract.get();
            performanceBoundCoefficient = (contract.getPerformanceBond() != null) ? contract.getPerformanceBond() : 0d;
            insuranceDepositCoefficient = (contract.getInsuranceDeposit() != null) ? contract.getInsuranceDeposit() : 0d;
        }

        NotInvoicedReportDto notInvoicedReportDto = getNotInvoicedByCustomerId(customerId, insuranceDepositCoefficient, performanceBoundCoefficient);
        AdjustmentReportDto adjustmentReportDto = getAdjustmentByCustomerId(customerId, insuranceDepositCoefficient, performanceBoundCoefficient);

        return new ClientSummaryResult(
                list,
                notInvoicedReportDto,
                adjustmentReportDto,
                establishmentDto,
                totalPaymentByCustomerId
        );
    }

    @Data
    private static class NotInvoiced {
        private Double amount;
        private Double quantity;

        public NotInvoiced(Double amount, Double quantity) {
            this.amount = amount;
            this.quantity = quantity;
        }
    }

    private NotInvoicedReportDto getNotInvoicedByCustomerId(Long customerId, Double insuranceDeposit, Double performanceBound) {
        List<Object[]> objectList = warehouseInvoiceRepository.getNotInvoicedAmountByCustomerId(customerId);
        NotInvoiced notInvoiced = objectList.stream().map(obj -> new NotInvoiced((Double) obj[0], (Double) obj[1])).toList().get(0);
        Double amount = notInvoiced.getAmount();
        Double quantity = notInvoiced.getQuantity();
        if (amount != null && amount > 0) {
            Double vat = (double) Math.round(amount * 0.09);
            Long performance = Math.round(amount * performanceBound);
            Long insurance = Math.round(amount * insuranceDeposit);

            return new NotInvoicedReportDto(amount, quantity, vat, insurance, performance);
        }
        return new NotInvoicedReportDto(0d, 0d, 0d, 0L, 0L);
    }

    private AdjustmentReportDto getAdjustmentByCustomerId(Long customerId, Double insuranceDeposit, Double performanceBound) {
        Double adjustments = (Double) warehouseReceiptItemRepository.getAdjustmentsByCustomerId(customerId);

        if (adjustments != null && adjustments != 0) {
            Double vat = (double) Math.round(adjustments * 0.09);
            Long insuranceDepositValue = Math.round(adjustments * insuranceDeposit);
            Long performanceBoundValue = Math.round(adjustments * performanceBound);

            return new AdjustmentReportDto(
                    adjustments,
                    vat,
                    insuranceDepositValue,
                    performanceBoundValue
            );
        }
        return new AdjustmentReportDto(0d, 0d, 0L, 0L);
    }

    private PaymentReportDto getPaymentGroupBySubjectFilterByCustomerId(Long customerId) {
        List<Object[]> objectList = paymentRepository.getPaymentGroupBySubjectFilterByCustomerId(customerId);

        PaymentReportDto paymentReportDto = new PaymentReportDto();

        for (Object[] row : objectList) {
            String paymentSubject = (String) row[0];
            Double sum = (Double) row[1];

            switch (paymentSubject) {
                case "PRODUCT" -> paymentReportDto.setProductPayment(sum);
                case "INSURANCEDEPOSIT" -> paymentReportDto.setInsuranceDepositPayment(sum);
                case "PERFORMANCEBOUND" -> paymentReportDto.setPerformanceBoundPayment(sum);
                case "ADVANCEDPAYMENT" -> paymentReportDto.setAdvancedPayment(sum);
            }
        }

        return paymentReportDto;
    }


    private EstablishmentDto getEstablishmentByCustomerId(Long customerId) {
        Optional<EstablishmentDto> optionalEstablishmentDto = establishmentService.getEstablishmentByCustomerId(customerId);

        return optionalEstablishmentDto.orElseGet(() -> new EstablishmentDto(0L, 0d, 0d, 0d, 0L));
    }

    public List<ClientSummaryDetailsDTO> getClientSummaryDetailsByCustomerId(Long customerId, String contractNumber) {
        List<Object[]> objects = warehouseReceiptItemRepository.getClientSummaryDetailsByContractNumber(customerId, contractNumber);
        List<ClientSummaryDetailsDTO> list = new ArrayList<>();

        for (Object[] obj : objects) {
            ClientSummaryDetailsDTO dto = new ClientSummaryDetailsDTO(
                    (String) obj[0],          // contractNumber
                    (Long) obj[1],            // id
                    (Long) obj[2],            // invoiceNumber
                    (String) obj[3],          // issuedDate
                    (Long) obj[4],            // totalQuantity
                    (Long) obj[5],            // totalAmount
                    (Long) obj[6],            // advancedPayment
                    (Long) obj[7],            // insuranceDeposit
                    (Long) obj[8]             // performanceBound
            );
            list.add(dto);
        }

        return list;
    }

    public List<NotInvoicedDetailsDto> getNotInvoicedAmountDetailsByCustomerIdAndYearName(Long customerId, Long yearName) {
        List<Object[]> objects = warehouseReceiptItemRepository.getNotInvoicedAmountDetailsByCustomerIdAndYearName(customerId, yearName);
        List<NotInvoicedDetailsDto> list = new ArrayList<>();
        for (Object[] obj : objects) {
            NotInvoicedDetailsDto dto = new NotInvoicedDetailsDto();
            dto.setReceiptNumber((Long) obj[0]);
            dto.setReceiptDate((String) obj[1]);
            dto.setReceiptNumber((Long) obj[2]);
            dto.setReceiptNumber((Long) obj[3]);

            list.add(dto);
        }
        return list;
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

    public CustomerDtoByQuery findCustomerById(Long id) {
        List<Object[]> list = customerRepository.findCustomerById(id);

        if (list == null) throw new EntityNotFoundException("مشتری با شناسه " + id + " یافت نشد.");
        CustomerDtoByQuery customer = new CustomerDtoByQuery();

        for (Object[] customerById : list) {
            customer.setId((Long) customerById[0]);
            customer.setName((String) customerById[1]);
            customer.setPhone((String) customerById[2]);
            customer.setCustomerCode((String) customerById[3]);
            customer.setEconomicCode((String) customerById[4]);
            customer.setNationalCode((String) customerById[5]);
            customer.setBigCustomer((Boolean) customerById[6]);
        }


        List<CustomerDtoByQuery.PaymentDto> payments = customerRepository.findPaymentsByCustomerId(id).stream()
                .map(obj -> {
                    CustomerDtoByQuery.PaymentDto paymentDto = new CustomerDtoByQuery.PaymentDto();
                    paymentDto.setId((Long) obj[0]);
                    paymentDto.setDescription((String) obj[1]);
                    paymentDto.setDate(((Date) obj[2]).toLocalDate());
                    paymentDto.setAmount((Long) obj[3]);
                    return paymentDto;
                })
                .filter(Objects::nonNull) // Filter out any null payments
                .toList(); // Assuming you are using Java 16 or later; otherwise, use Collectors.toList()

        customer.getPayments().addAll(payments);

        return customer;
    }

    public CustomerDto createCustomer(CustomerDto customerDto) {
        Customer customerRepositoryByNationalCode = customerRepository.findByNationalCode(customerDto.getNationalCode());
        if (customerRepositoryByNationalCode != null) {
            throw new DatabaseIntegrityViolationException("کد ملی " + customerRepositoryByNationalCode.getNationalCode() + "قبلا برای " + customerRepositoryByNationalCode.getName() + "ثبت شده است.");
        }
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
        Long customerId = optionalCustomer.get().getId();

        if (customerRepository.existsCustomerByIdAndReportItemsIsNotEmpty(customerId)) {
            throw new DatabaseIntegrityViolationException("امکان حذف مشتری وجود ندارد چون آیتم‌های گزارش مرتبط دارد.");
        }
        if (customerRepository.existsCustomerByIdAndWarehouseReceiptsIsNotEmpty(id)) {
            throw new DatabaseIntegrityViolationException("امکان حذف مشتری وجود ندارد چون رسیدهای انبار مرتبط دارد.");
        }
        if (customerRepository.existsCustomerByIdAndContractsIsNotEmpty(customerId)) {
            throw new DatabaseIntegrityViolationException("امکان حذف مشتری وجود ندارد چون قراردادها مرتبط دارد.");
        }
        if (customerRepository.existsCustomerByIdAndInvoicesIsNotEmpty(id)) {
            throw new DatabaseIntegrityViolationException("امکان حذف مشتری وجود ندارد چون فاکتورهای مرتبط دارد.");
        }
        if (customerRepository.existsCustomerByIdAndPaymentsIsNotEmpty(customerId)) {
            throw new DatabaseIntegrityViolationException("امکان حذف مشتری وجود ندارد چون پرداخت‌های مرتبط دارد.");
        }
        customerRepository.deleteById(customerId);
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

    protected LocalDate convertDate(String jalaliDateStr) {
        DateConverter dateConverter = new DateConverter();
        String[] dateParts = jalaliDateStr.split("/");
        if (dateParts.length == 3) {
            int jalaliYear = Integer.parseInt(dateParts[0]);
            int jalaliMonth = Integer.parseInt(dateParts[1]);
            int jalaliDay = Integer.parseInt(dateParts[2]);

            return dateConverter.jalaliToGregorian(jalaliYear, jalaliMonth, jalaliDay);
        }

        return null;
    }

    @Transactional
    public void importCustomerPaymentsFromExcel(MultipartFile file) throws IOException {
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            Map<String, Customer> customerMap = new HashMap<>();
            for (Row row : sheet) {
                if (row.getRowNum() == 0) {
                    continue;
                }

                try {
                    int rowNum = row.getRowNum() + 1;
                    String customerCode = row.getCell(0).getStringCellValue();
                    Customer customer = customerMap.get(customerCode);
                    if (customer == null) {
                        customer = customerRepository.findByCustomerCode(String.valueOf(customerCode)).orElseThrow(() -> new EntityNotFoundException("مشتری با کد " + customerCode + " یافت نشد."));
                    } else {
                        customerMap.put(customerCode, customer);
                    }
                    Payment payment = new Payment();
                    payment.setDate(convertDate(row.getCell(1).getStringCellValue()));
                    payment.setDescription(row.getCell(2).getStringCellValue());
                    payment.setAmount((long) row.getCell(3).getNumericCellValue());
                    payment.setCustomer(customer);
                    long yearName = (long) row.getCell(4).getNumericCellValue();
                    Optional<Year> optionalYear = yearRepository.findByYearName(yearName);
                    if (optionalYear.isEmpty())
                        throw new EntityNotFoundException("سال با شناسه " + yearName + " یافت نشد.");
                    payment.setYear(optionalYear.get());
                    String subjectCellValue = row.getCell(5) != null ? row.getCell(5).getStringCellValue() : null;

                    if (subjectCellValue == null || subjectCellValue.trim().isEmpty() || !isValidSubject(subjectCellValue)) {
                        subjectCellValue = "PRODUCT";
                    }

                    payment.setSubject(subjectCellValue);

                    customer.getPayments().add(payment);
                } catch (Exception e) {
                    e.printStackTrace();
                    int rowNum = row.getRowNum() + 1;
                    throw new RowImportException(rowNum, e.getMessage());
                }
            }
            customerRepository.saveAll(customerMap.values());
        }
    }

    private boolean isValidSubject(String subject) {
        // Implement your validation logic here
        // For example, check if the subject is one of the valid options
        return Arrays.asList("PRODUCT", "INSURANCEDEPOSIT", "PERFORMANCEBOUND").contains(subject);
    }


    public CustomerTotalsDto calculateCustomerTotals(Long customerId, Long yearName) {
        List<Object[]> list = customerRepository.calculateCustomerTotals(customerId, yearName);
        return list.stream().map(obj -> {
            CustomerTotalsDto dto = new CustomerTotalsDto();
            dto.setCustomerId((Long) obj[0]);
            dto.setCustomerName((String) obj[1]);
            dto.setTotalPayment((Long) obj[2]);
            dto.setTotalAmount((Long) obj[3]);
            dto.setTotalQuantity((Long) obj[4]);
            dto.setTotalInvoiceCount((Long) obj[5]);
            return dto;
        }).toList().get(0);
    }

    public List<MonthlyCustomerInvoiceSummaryDTO> getCustomerInvoiceMonthlySummary(Long customerId, Long yearName) {
        List<Object[]> resultList = customerRepository.getCustomerInvoiceMonthlySummary(customerId, yearName);
        return mapToObjectDTOList(resultList);
    }

    private List<MonthlyCustomerInvoiceSummaryDTO> mapToObjectDTOList(List<Object[]> resultList) {
        List<MonthlyCustomerInvoiceSummaryDTO> dtoList = new ArrayList<>();
        for (Object[] result : resultList) {
            dtoList.add(mapToObjectDTO(result));
        }
        return dtoList;
    }

    private MonthlyCustomerInvoiceSummaryDTO mapToObjectDTO(Object[] result) {
        return new MonthlyCustomerInvoiceSummaryDTO(
                (Integer) result[0],          // monthNumber
                (String) result[1],           // persianCaption
                (Long) result[2],             // paymentAmount
                (Long) result[3],             // invoiceCount
                (Long) result[4],             // totalAmount
                (Long) result[5]              // totalQuantity
        );
    }

    public List<InvoiceUploadDto> mapToInvoiceUploadDtoList(Long customerId) {
        List<Object[]> resultList = customerRepository.getInvoiceUploadDto(customerId);
        List<InvoiceUploadDto> invoiceUploadDtoList = new ArrayList<>();

        for (Object[] result : resultList) {
            InvoiceUploadDto dto = new InvoiceUploadDto();
            dto.setInvoiceId((Long) result[0]);
            dto.setInvoiceNumber((Long) result[1]);
            dto.setIssuedDate((String) result[2]);
            dto.setDueDate((String) result[3]);
            dto.setSalesType((String) result[4]);
            dto.setContractNumber((String) result[5]);
            dto.setStatusId((Integer) result[6]);
            dto.setYear((Long) result[7]);
            dto.setCustomerCode((String) result[8]);
            dto.setAdvancedPayment((Long) result[9]);
            dto.setPerformanceBound((Long) result[10]);
            dto.setInsuranceDeposit((Long) result[11]);
            dto.setProductType((String) result[12]);
            dto.setQuantity((Integer) result[13]);
            dto.setUnitPrice((Long) result[14]);
            dto.setWarehouseReceiptNumber((Long) result[15]);
            dto.setWarehouseReceiptDate((String) result[16]);

            invoiceUploadDtoList.add(dto);
        }

        return invoiceUploadDtoList;
    }

    public XSSFWorkbook generateInvoiceUploadExcel(Long customerId) {
        // Create a new Excel workbook
        XSSFWorkbook workbook = new XSSFWorkbook();

        // Create an Excel sheet
        XSSFSheet sheet = workbook.createSheet("لیست فاکتورها");

        // Create a header row
        String[] headers = {"شناسه فاکتور", "شماره فاکتور", "تاریخ صدور", "تاریخ سررسید", "نوع فروش", "شماره قرارداد", "شناسه وضعیت", "سال", "کد مشتری", "پیش پرداخت", "حسن انجام کار", "سپرده بیمه", "نوع محصول", "تعداد", "قیمت واحد", "شماره رسید انبار", "تاریخ رسید انبار"};
        Row headerRow = sheet.createRow(0);
        CellStyle headerCellStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerCellStyle.setFont(headerFont);

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerCellStyle);
        }

        // Fetch invoice data using the mapToInvoiceUploadDtoList method
        List<InvoiceUploadDto> invoices = mapToInvoiceUploadDtoList(customerId);

        // Create rows for each invoice
        int rowNum = 1;
        for (InvoiceUploadDto invoice : invoices) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(invoice.getInvoiceId());
            row.createCell(1).setCellValue(invoice.getInvoiceNumber());
            row.createCell(2).setCellValue(invoice.getIssuedDate());
            row.createCell(3).setCellValue(invoice.getDueDate());
            row.createCell(4).setCellValue(invoice.getSalesType());
            row.createCell(5).setCellValue(invoice.getContractNumber());
            row.createCell(6).setCellValue(invoice.getStatusId());
            row.createCell(7).setCellValue(invoice.getYear());
            row.createCell(8).setCellValue(invoice.getCustomerCode());
            row.createCell(9).setCellValue(invoice.getAdvancedPayment());
            row.createCell(10).setCellValue(invoice.getPerformanceBound());
            row.createCell(11).setCellValue(invoice.getInsuranceDeposit());
            row.createCell(12).setCellValue(invoice.getProductType());
            row.createCell(13).setCellValue(invoice.getQuantity());
            row.createCell(14).setCellValue(invoice.getUnitPrice());
            row.createCell(15).setCellValue(invoice.getWarehouseReceiptNumber());
            row.createCell(16).setCellValue(invoice.getWarehouseReceiptDate());
        }

        // Auto-size columns for better readability
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        return workbook;
    }
}

