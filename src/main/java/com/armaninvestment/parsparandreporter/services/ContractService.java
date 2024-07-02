package com.armaninvestment.parsparandreporter.services;


import com.armaninvestment.parsparandreporter.dtos.*;
import com.armaninvestment.parsparandreporter.entities.*;
import com.armaninvestment.parsparandreporter.exceptions.DatabaseIntegrityViolationException;
import com.armaninvestment.parsparandreporter.exceptions.RowImportException;
import com.armaninvestment.parsparandreporter.mappers.ContractListMapper;
import com.armaninvestment.parsparandreporter.mappers.ContractMapper;
import com.armaninvestment.parsparandreporter.mappers.ContractSummaryMapper;
import com.armaninvestment.parsparandreporter.repositories.ContractRepository;
import com.armaninvestment.parsparandreporter.repositories.CustomerRepository;
import com.armaninvestment.parsparandreporter.repositories.ProductRepository;
import com.armaninvestment.parsparandreporter.repositories.YearRepository;
import com.github.eloyzone.jalalicalendar.DateConverter;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ContractService {
    private final ContractRepository contractRepository;
    private final ContractMapper contractMapper;
    private final ContractListMapper contractListMapper;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final ContractSummaryMapper contractSummaryMapper;
    private final YearRepository yearRepository;


    @Autowired
    public ContractService(ContractRepository contractRepository,
                           ContractMapper contractMapper,
                           ContractListMapper contractListMapper, ProductRepository productRepository,
                           CustomerRepository customerRepository,
                           ContractSummaryMapper contractSummaryMapper, YearRepository yearRepository) {
        this.contractRepository = contractRepository;
        this.contractMapper = contractMapper;
        this.contractListMapper = contractListMapper;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.contractSummaryMapper = contractSummaryMapper;
        this.yearRepository = yearRepository;
    }

    public String findContractDescriptionByContractNumber(String contractNumber) {
        String contractDescriptionByContractNumber = contractRepository.findContractDescriptionByContractNumber(contractNumber);
        return (contractDescriptionByContractNumber != null) ? contractDescriptionByContractNumber : "نقدی";
    }


    public List<ContractListDto> getAllContracts(Long customerId) {
        List<Contract> contracts;

        if (customerId != null) {
            // If customerId is provided, get contracts based on customerId
            contracts = contractRepository.findAllByCustomer(new Customer(customerId));
        } else {
            // If customerId is not provided, get all contracts
            contracts = contractRepository.findAll();
        }

        return contracts.stream().map(contractListMapper::toDto).collect(Collectors.toList());
    }

    public List<ContractSummaryDto> getAllContractSummaries() {
        return contractRepository.findAll().stream().map(contractSummaryMapper::toDto).collect(Collectors.toList());
    }

    public List<ContractListDto> searchContractsForDropdown(String searchQuery, Long customerId) {
        List<Contract> matchingStatuses;

        if (customerId != null) {
            matchingStatuses = contractRepository.findByContractDescriptionContainsAndCustomerId(searchQuery, customerId);
        } else {
            matchingStatuses = contractRepository.findByContractDescriptionContains(searchQuery);
        }

        return matchingStatuses.stream()
                .map(contractListMapper::toDto)
                .toList();
    }

    @Transactional
    public ContractDto create(ContractDto contractDto) {
        Optional<Year> optionalYear = yearRepository.findByYearName(contractDto.getYearId());
        if (optionalYear.isEmpty()) {
            throw new EntityNotFoundException("سال با شناسه " + contractDto.getYearId() + " یافت نشد.");
        }
        Contract contract = contractMapper.toEntity(contractDto);
        contract.setYear(optionalYear.get());
        Contract savedContract = contractRepository.save(contract);
        return contractMapper.toDto(savedContract);
    }

    public ContractDto getContractById(Long contractId) {
        Optional<Contract> optionalContract = contractRepository.findById(contractId);
        if (optionalContract.isEmpty()) {
            throw new EntityNotFoundException("قرارداد با شناسه " + contractId + " یافت نشد.");
        }

        return contractMapper.toDto(optionalContract.get());
    }

    private ContractDtoByQuery mapToContractDto(List<Object[]> contract, List<Object[]> contractItem) {
        if (contract.isEmpty()) {
            return null;
        }
        Object[] row = contract.get(0);
        ContractDtoByQuery contractDto = new ContractDtoByQuery();

        contractDto.setContractId((Long) row[0]);
        contractDto.setContractNumber((String) row[1]);
        contractDto.setContractDescription((String) row[2]);
        contractDto.setStartDate(((Date) row[3]).toLocalDate());
        contractDto.setEndDate(((Date) row[4]).toLocalDate());
        contractDto.setAdvancePaymentCoefficient((Double) row[5]);
        contractDto.setContractPerformanceBondCoefficient((Double) row[6]);
        contractDto.setContractInsuranceDepositCoefficient((Double) row[7]);
        contractDto.setCustomerId((Long) row[8]);
        contractDto.setYearId((Long) row[9]);
        contractDto.setCumulativeContractAmount((Long) row[10]);
        contractDto.setCumulativeContractQuantity((Long) row[11]);
        contractDto.setContractAdvancedPaymentAmount((Long) row[12]);
        contractDto.setContractPerformanceBoundAmount((Long) row[13]);
        contractDto.setContractInsuranceDepositAmount((Long) row[14]);
        contractDto.setTotalCommittedContractAmount((Long) row[15]);
        contractDto.setTotalCommittedContractCount((Long) row[16]);
        contractDto.setTotalRemainingContractAmount((Long) row[17]);
        contractDto.setTotalRemainingContractCount((Long) row[18]);
        contractDto.setTotalConsumedContractAdvancedPayment((Long) row[19]);
        contractDto.setTotalOutstandingContractAdvancedPayment((Long) row[20]);
        contractDto.setTotalCommittedPerformanceBound((Long) row[21]);
        contractDto.setTotalRemainingPerformanceBond((Long) row[22]);
        contractDto.setTotalCommittedInsuranceDeposit((Long) row[23]);
        contractDto.setTotalRemainingInsuranceDeposit((Long) row[24]);

        List<ContractDtoByQuery.ContractItemDto> contractItems = mapToContractItemDtoSet(contractItem);
        contractDto.setContractItems(new LinkedHashSet<>(contractItems));
        return contractDto;
    }

    private List<ContractDtoByQuery.ContractItemDto> mapToContractItemDtoSet(List<Object[]> result) {
        return result.stream()
                .map(this::mapToContractItemDto)
                .collect(Collectors.toList());
    }

    private ContractDtoByQuery.ContractItemDto mapToContractItemDto(Object[] row) {
        Long itemId = (Long) row[0];
        Long unitPrice = (Long) row[1];
        Long quantity = (Long) row[2];
        Long productId = (Long) row[3];

        return new ContractDtoByQuery.ContractItemDto(itemId, unitPrice, quantity, productId);
    }

    public ContractListDto getContractListById(Long id) {
        Optional<Contract> optionalContract = contractRepository.findById(id);
        if (optionalContract.isPresent()) {
            Contract contract = optionalContract.get();
            return contractListMapper.toDto(contract);
        } else {
            return null;
        }
    }

    public ContractDto updateContract(Long id, ContractDto contractDto) {
        Optional<Contract> optionalContract = contractRepository.findById(id);
        if (optionalContract.isPresent()) {
            Contract contract = optionalContract.get();
            Contract partialUpdate = contractMapper.partialUpdate(contractDto, contract);
            Contract saved = contractRepository.save(partialUpdate);
            return contractMapper.toDto(saved);
        }
        return null;
    }

    public Contract findByContractNumber(String contractNumber) {
        return contractRepository.findByContractNumber(contractNumber).orElseThrow(() -> new EntityNotFoundException("قرارداد با شماره قرارداد " + contractNumber + " یافت نشد."));
    }

    public List<ContractListDto> findAllByYearName(Long yearName) {
        Optional<Year> optionalYear = yearRepository.findByYearName(yearName);
        if (optionalYear.isEmpty()) {
            throw new EntityNotFoundException("سال با شناسه " + yearName + " یافت نشد.");
        }
        return contractRepository.mapToContractListDto(yearName);
    }

    public void deleteContract(Long id) {
        if (!contractRepository.existsContractById(id)) {
            throw new EntityNotFoundException("قراردادی با شناسه " + id + "یافت نشد.");
        }
        if (contractRepository.existsContractByIdAndInvoicesIsNotEmpty(id)) {
            throw new DatabaseIntegrityViolationException("امکان حذف قرارداد وجود ندارد چون فاکتور های مرتبط دارد.");
        }
        contractRepository.deleteById(id);
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
    public void importContracts(MultipartFile file) throws IOException {
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0); // Assuming data is in the first sheet

            Map<String, ContractDto> contractMap = new HashMap<>();

            for (Row row : sheet) {
                if (row.getRowNum() == 0) {
                    continue; // Skip header row
                }
                try {
                    // Adjust row number to be 1-based
                    String contractNumber = row.getCell(0).getStringCellValue();

                    // Check if the contract with this number already exists
                    ContractDto contract = contractMap.get(contractNumber);
                    if (contract == null) {
                        // If not, create a new contract
                        contract = new ContractDto();
                        contract.setContractNumber(contractNumber);
                        contract.setContractDescription(row.getCell(1).getStringCellValue());
                        contract.setStartDate(convertDate(row.getCell(2).getStringCellValue()));
                        contract.setEndDate(convertDate(row.getCell(3).getStringCellValue()));
                        contract.setAdvancePayment(row.getCell(4).getNumericCellValue());
                        contract.setPerformanceBond(row.getCell(5).getNumericCellValue());
                        contract.setInsuranceDeposit(row.getCell(6).getNumericCellValue());
                        long customerCode = (long) row.getCell(7).getNumericCellValue();
                        contract.setCustomerId(customerRepository
                                .findByCustomerCode(String.valueOf(customerCode))
                                .orElseThrow(() -> new EntityNotFoundException("مشتری با این کد " + customerCode + "یافت نشد."))
                                .getId());
                        contract.setYearId((long) row.getCell(8).getNumericCellValue());
                        contract.setContractItems(new HashSet<>());
                        contractMap.put(contractNumber, contract);
                    }

                    // Create and add contract items to the contract
                    ContractItemDto contractItem = new ContractItemDto();
                    contractItem.setUnitPrice((long) row.getCell(9).getNumericCellValue());
                    contractItem.setQuantity((long) row.getCell(10).getNumericCellValue());
                    String productCode = String.valueOf((long) row.getCell(11).getNumericCellValue());
                    Product product = productRepository
                            .findByProductCode(productCode)
                            .orElseThrow(() -> new EntityNotFoundException("محصولی با کد " + productCode + "یافت نشد."));
                    contractItem.setProductId(product.getId());
                    contract.getContractItems().add(contractItem);
                } catch (Exception e) {
                    // Catch exceptions that may occur while processing the row
                    int rowNum = row.getRowNum() + 1; // Adjust row number to be 1-based
                    throw new RowImportException(rowNum, e.getMessage());
                }
            }

            // Save contracts to the database
            for (ContractDto c : contractMap.values()) {
                contractRepository.save(contractMapper.toEntity(c));
            }
        }
    }

    public XSSFWorkbook generateContractListExcel() {
        // Create a new Excel workbook
        XSSFWorkbook workbook = new XSSFWorkbook();

        // Create an Excel sheet
        XSSFSheet sheet = workbook.createSheet("لیست قرارداد ها");

        // Create a header row
        String[] headers = {"شماره قرارداد", "عنوان قرارداد", "تاریخ شروع", "تاریخ خاتمه", "درصد پیش پرداخت", "درصد حسن انجام کار", "درصد سپرده بیمه", "شناسه مشتری"};
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

        // Fetch contract data from your database
        List<ContractDto> contracts = contractRepository.findAll().stream().map(contractMapper::toDto).toList();

        // Create rows for each contract
        int rowNum = 1;
        for (ContractDto contract : contracts) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(contract.getContractNumber());
            row.createCell(1).setCellValue(contract.getContractDescription());
            row.createCell(2).setCellValue(contract.getStartDate().toString());
            row.createCell(3).setCellValue(contract.getEndDate().toString());
            row.createCell(4).setCellValue(contract.getAdvancePayment());
            row.createCell(5).setCellValue(contract.getPerformanceBond());
            row.createCell(6).setCellValue(contract.getInsuranceDeposit());
            row.createCell(7).setCellValue(contract.getCustomerId());
        }
        // Auto-size columns for better readability
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        return workbook;
    }

    public List<InvoicesByContractIdDto> getAllInvoicesByContractId(Long contractId) {
        List<Object[]> list = contractRepository.getAllInvoicesByContractId(contractId);
        return list.stream().map(obj -> {
            InvoicesByContractIdDto dto = new InvoicesByContractIdDto();
            dto.setInvoiceId((Long) obj[0]);
            dto.setInvoiceNumber((Long) obj[1]);
            dto.setInvoiceDate((String) obj[2]);
            dto.setInvoiceQuantity((Long) obj[3]);
            dto.setInvoiceAmount((Long) obj[4]);
            dto.setInvoiceAddedValueTax((Long) obj[5]);
            dto.setAdvancedPayment((Long) obj[6]);
            dto.setPerformanceBound((Long) obj[7]);
            dto.setInsuranceDeposit((Long) obj[8]);
            return dto;
        }).toList();
    }
}
