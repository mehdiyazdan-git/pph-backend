package com.armaninvestment.parsparandreporter.services;

import com.armaninvestment.parsparandreporter.dtos.ContractAndInvoiceTotalsDTO;
import com.armaninvestment.parsparandreporter.dtos.InvoiceDto;
import com.armaninvestment.parsparandreporter.dtos.InvoiceItemDto;
import com.armaninvestment.parsparandreporter.dtos.InvoiceListDto;
import com.armaninvestment.parsparandreporter.entities.*;
import com.armaninvestment.parsparandreporter.enums.SalesType;
import com.armaninvestment.parsparandreporter.exceptions.WarehouseReceiptNotFoundException;
import com.armaninvestment.parsparandreporter.mappers.InvoiceListMapper;
import com.armaninvestment.parsparandreporter.mappers.InvoiceMapper;
import com.armaninvestment.parsparandreporter.repositories.*;
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
import java.io.InterruptedIOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class InvoiceService {
    private final InvoiceRepository invoiceRepository;
    private final InvoiceMapper invoiceMapper;
    private final InvoiceListMapper invoiceListMapper;
    private final WarehouseReceiptRepository warehouseReceiptRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final ContractRepository contractRepository;
    private final YearRepository yearRepository;

    @Autowired
    public InvoiceService(InvoiceRepository invoiceRepository, InvoiceMapper invoiceMapper, InvoiceListMapper invoiceListMapper,
                          WarehouseReceiptRepository warehouseReceiptRepository,
                          CustomerRepository customerRepository, ProductRepository productRepository, ContractRepository contractRepository, YearRepository yearRepository) {
        this.invoiceRepository = invoiceRepository;
        this.invoiceMapper = invoiceMapper;
        this.invoiceListMapper = invoiceListMapper;
        this.warehouseReceiptRepository = warehouseReceiptRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.contractRepository = contractRepository;
        this.yearRepository = yearRepository;
    }

    public Long getMaxInvoiceNumber() {
        return invoiceRepository.findMaxInvoiceNumber();
    }

    @Transactional
    public InvoiceDto createInvoice(InvoiceDto invoiceDto) {
        Invoice invoice = invoiceMapper.toEntity(invoiceDto);
        return invoiceMapper.toDto(invoiceRepository.save(invoice));
    }

    public List<InvoiceListDto> findAllInvoicesByContractId(Long contractId) {
        return invoiceRepository.findAllByContractIdSortedByInvoiceNumberAsc(contractId).stream().map(invoiceListMapper::toDto).collect(Collectors.toList());
    }

    public List<InvoiceListDto> findAllInvoices() {
        return invoiceRepository.findAll().stream().map(invoiceListMapper::toDto).collect(Collectors.toList());
    }

    public List<InvoiceListDto> findAllInvoicesByYearName(Long yearName) {
        Optional<Year> optionalYear = yearRepository.findByYearName(yearName);
        if (optionalYear.isEmpty()) {
            throw new EntityNotFoundException("سال با شناسه " + yearName + " یافت نشد.");
        }
        return invoiceRepository.findAllByYearOrderByIssuedDateAsc(new Year(optionalYear.get().getId()))
                .stream()
                .map(invoiceListMapper::toDto)
                .collect(Collectors.toList());
    }

    public ContractAndInvoiceTotalsDTO getContractDetailsByContractIdAndInvoiceId(Long contractId, Long invoiceId) {
        return invoiceRepository.getMappedContractAndInvoiceTotals(contractId, invoiceId);
    }


    public Optional<InvoiceDto> getInvoiceById(Long id) {
        return invoiceRepository.findById(id).map(invoiceMapper::toDto);
    }

    @Transactional
    public InvoiceDto updateInvoice(Long id, InvoiceDto updatedInvoiceDto) {
        Optional<Invoice> existingInvoiceOptional = invoiceRepository.findById(id);
        if (existingInvoiceOptional.isPresent()) {
            Invoice existingInvoice = existingInvoiceOptional.get();
            Invoice updatedInvoice = invoiceMapper.partialUpdate(updatedInvoiceDto, existingInvoice);
            Invoice saved = invoiceRepository.save(updatedInvoice);
            return invoiceMapper.toDto(saved);
        }
        return null;
    }

    public void deleteInvoice(Long id) throws InterruptedIOException {
        Optional<Invoice> optionalInvoice = invoiceRepository.findById(id);
        if (optionalInvoice.isEmpty()) {
            throw new EntityNotFoundException("فاکتوری ای با شناسه " + id + "یافت نشد.");
        }
        invoiceRepository.deleteById(id);
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
    public void importInvoicesFromExcel(MultipartFile file) throws IOException {
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0); // Assuming data is in the first sheet

            Map<Long, InvoiceDto> invoiceMap = new HashMap<>();

            for (Row row : sheet) {
                if (row.getRowNum() == 0) {
                    continue; // Skip the header row
                }

                Long invoiceNumber = (long) row.getCell(0).getNumericCellValue();

                // Check if the invoiceDto with this ID already exists
                InvoiceDto invoiceDto = invoiceMap.get(invoiceNumber);
                if (invoiceDto == null) {
                    // If not, create a new invoiceDto
                    invoiceDto = new InvoiceDto();
                    invoiceDto.setInvoiceNumber(invoiceNumber);
                    invoiceDto.setIssuedDate(convertDate(row.getCell(1).getStringCellValue()));
                    invoiceDto.setDueDate(convertDate(row.getCell(2).getStringCellValue()));

                    String salesType = row.getCell(3).getStringCellValue();
                    invoiceDto.setSalesType(SalesType.valueOf(salesType));

                    if (Objects.equals(salesType, SalesType.CONTRACTUAL_SALES.name())) {
                        String contractNumber = row.getCell(4).getStringCellValue();
                        Optional<Contract> optionalContract = contractRepository.findByContractNumber(contractNumber);
                        if (optionalContract.isEmpty()) {
                            throw new EntityNotFoundException("قرارداد با شماره قرارداد " + contractNumber + " یافت نشد.");
                        } else {
                            Contract contract = optionalContract.get();
                            invoiceDto.setContractId(contract.getId());
                        }
                    } else {
                        invoiceDto.setContractId(null);
                    }
                    invoiceDto.setInvoiceStatusId((int) row.getCell(5).getNumericCellValue());
                    invoiceDto.setYearName((long) row.getCell(6).getNumericCellValue());
                    long customerCode = (long) row.getCell(7).getNumericCellValue();
                    Customer customer = customerRepository.findByCustomerCode(String.valueOf(customerCode)).orElseThrow(() -> new EntityNotFoundException("مشتری با کد " + customerCode + " یافت نشد."));
                    invoiceDto.setCustomerId(customer.getId());
                    invoiceDto.setInvoiceItems(new LinkedHashSet<>());
                    invoiceMap.put(invoiceNumber, invoiceDto);
                }

                // Create and add invoiceDto items to the invoiceDto
                InvoiceItemDto invoiceItemDto = new InvoiceItemDto();
                String productCode = String.valueOf((long) row.getCell(8).getNumericCellValue());
                Product product = productRepository
                        .findByProductCode(productCode)
                        .orElseThrow(() -> new EntityNotFoundException("محصولی با کد " + productCode + "یافت نشد."));
                invoiceItemDto.setProductId(product.getId());
                invoiceItemDto.setQuantity((int) row.getCell(9).getNumericCellValue());
                invoiceItemDto.setUnitPrice((long) row.getCell(10).getNumericCellValue());
                long receiptNumber = (long) row.getCell(11).getNumericCellValue();
                LocalDate receiptDate = convertDate(row.getCell(12).getStringCellValue());
                invoiceItemDto.setWarehouseReceiptId(findWarehouseReceiptByNumberAndDate(receiptNumber, receiptDate).getId());
                invoiceDto.getInvoiceItems().add(invoiceItemDto);
            }
            // Save invoices to the database
            for (InvoiceDto i : invoiceMap.values()) {
                invoiceRepository.save(invoiceMapper.toEntity(i));
            }

        }
    }

    public WarehouseReceipt findWarehouseReceiptByNumberAndDate(Long number, LocalDate date) {
        WarehouseReceipt warehouseReceipt = warehouseReceiptRepository.findByNumberAndDate(number, date);
        if (warehouseReceipt == null) {
            throw new WarehouseReceiptNotFoundException("حواله با شناسه " + number + " و تاریخ " + date + " یافت نشد.");
        }
        return warehouseReceipt;
    }

    public XSSFWorkbook generateInvoicesToExcel(List<InvoiceDto> invoices) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Invoices");

        String[] headers = {"شناسه", "شماره صورت حساب", "تاریخ صدور", "تاریخ پیگیری", "شماره قرارداد", "وضعیت", "کد محصول", "تعداد", "مبلغ واحد(ریال)", "شناسه حواله"};
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

        int rowNum = 1;
        for (InvoiceDto invoice : invoices) {
            for (InvoiceItemDto invoiceItem : invoice.getInvoiceItems()) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(invoice.getId());
                row.createCell(1).setCellValue(invoice.getInvoiceNumber());
                row.createCell(2).setCellValue(invoice.getIssuedDate().toString());
                row.createCell(3).setCellValue(invoice.getDueDate().toString());
                row.createCell(4).setCellValue(invoice.getContractId());
                row.createCell(5).setCellValue(invoice.getInvoiceStatusId());
                row.createCell(6).setCellValue(invoiceItem.getProductId());
                row.createCell(7).setCellValue(invoiceItem.getQuantity());
                row.createCell(8).setCellValue(invoiceItem.getUnitPrice());
                row.createCell(9).setCellValue(invoiceItem.getWarehouseReceiptId());
            }
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        return workbook;
    }


}

