package com.armaninvestment.parsparandreporter.services;

import com.armaninvestment.parsparandreporter.dtos.NotInvoiced;
import com.armaninvestment.parsparandreporter.dtos.WarehouseReceiptDto;
import com.armaninvestment.parsparandreporter.dtos.WarehouseReceiptItemDto;
import com.armaninvestment.parsparandreporter.dtos.list.WarehouseReceiptListDto;
import com.armaninvestment.parsparandreporter.entities.Customer;
import com.armaninvestment.parsparandreporter.entities.Product;
import com.armaninvestment.parsparandreporter.entities.WarehouseReceipt;
import com.armaninvestment.parsparandreporter.entities.Year;
import com.armaninvestment.parsparandreporter.exceptions.DatabaseIntegrityViolationException;
import com.armaninvestment.parsparandreporter.exceptions.RowImportException;
import com.armaninvestment.parsparandreporter.mappers.WarehouseReceiptMapper;
import com.armaninvestment.parsparandreporter.mappers.list.WarehouseReceiptListMapper;
import com.armaninvestment.parsparandreporter.repositories.CustomerRepository;
import com.armaninvestment.parsparandreporter.repositories.ProductRepository;
import com.armaninvestment.parsparandreporter.repositories.WarehouseReceiptRepository;
import com.armaninvestment.parsparandreporter.repositories.YearRepository;
import com.github.eloyzone.jalalicalendar.DateConverter;
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
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class WarehouseReceiptService {

    private final WarehouseReceiptRepository warehouseReceiptRepository;
    private final WarehouseReceiptMapper warehouseReceiptMapper;
    private final ProductRepository productRepository;
    private final WarehouseReceiptListMapper warehouseReceiptListMapper;
    private final CustomerRepository customerRepository;
    private final YearRepository yearRepository;

    @Autowired
    public WarehouseReceiptService(WarehouseReceiptRepository warehouseReceiptRepository,
                                   WarehouseReceiptMapper warehouseReceiptMapper,
                                   ProductRepository productRepository,
                                   WarehouseReceiptListMapper warehouseReceiptListMapper,
                                   CustomerRepository customerRepository,
                                   YearRepository yearRepository) {
        this.warehouseReceiptRepository = warehouseReceiptRepository;
        this.warehouseReceiptMapper = warehouseReceiptMapper;
        this.productRepository = productRepository;
        this.warehouseReceiptListMapper = warehouseReceiptListMapper;
        this.customerRepository = customerRepository;
        this.yearRepository = yearRepository;
    }

    public List<NotInvoiced> findNotInvoicedByYearAndCustomer(String customerCode, Long yearName, Boolean invoiced) {
        if (customerCode != null) {
            Optional<Customer> optionalCustomer = customerRepository.findByCustomerCode(customerCode);
            if (optionalCustomer.isEmpty()) {
                throw new EntityNotFoundException("مشتری با کد " + customerCode + " یافت نشد.");
            }
        }

        if (yearName != null) {
            Optional<Year> optionalYear = yearRepository.findByYearName(yearName);
            if (optionalYear.isEmpty()) {
                throw new EntityNotFoundException("سال با شناسه " + yearName + " یافت نشد.");
            }
        }

        return warehouseReceiptRepository.mapToNotInvoicedList(customerCode, yearName, invoiced);
    }


    public List<WarehouseReceiptDto> getAllWarehouseReceipts() {
        return warehouseReceiptRepository.findAll().stream().map(warehouseReceiptMapper::toDto).collect(Collectors.toList());
    }

    public List<WarehouseReceiptListDto> getWarehouseReceiptList(Long yearName) {
        Optional<Year> optionalYear = yearRepository.findByYearName(yearName);
        if (optionalYear.isEmpty()) {
            throw new EntityNotFoundException("سال با شناسه " + yearName + " یافت نشد.");
        }
        return warehouseReceiptRepository.findAllByYearOrderByWarehouseReceiptDate(new Year(optionalYear.get().getId()))
                .stream()
                .map(warehouseReceiptListMapper::toDto)
                .collect(Collectors.toList());
    }


    public Optional<WarehouseReceiptDto> getWarehouseReceiptById(Long id) {
        Optional<WarehouseReceipt> warehouseReceipt = warehouseReceiptRepository.findById(id);
        return warehouseReceipt.map(warehouseReceiptMapper::toDto);
    }

    public WarehouseReceiptDto createWarehouseReceipt(WarehouseReceiptDto warehouseReceiptDto) {
        WarehouseReceipt warehouseReceipt = warehouseReceiptMapper.toEntity(warehouseReceiptDto);
        WarehouseReceipt savedWarehouseReceipt = warehouseReceiptRepository.save(warehouseReceipt);
        return warehouseReceiptMapper.toDto(savedWarehouseReceipt);
    }

    public Optional<WarehouseReceiptDto> updateWarehouseReceipt(Long id, WarehouseReceiptDto warehouseReceiptDto) {
        Optional<WarehouseReceipt> existingWarehouseReceipt = warehouseReceiptRepository.findById(id);
        if (existingWarehouseReceipt.isPresent()) {
            WarehouseReceipt warehouseReceipt = existingWarehouseReceipt.get();
            warehouseReceiptMapper.partialUpdate(warehouseReceiptDto, warehouseReceipt);
            WarehouseReceipt updatedWarehouseReceipt = warehouseReceiptRepository.save(warehouseReceipt);
            return Optional.of(warehouseReceiptMapper.toDto(updatedWarehouseReceipt));
        }
        return Optional.empty();
    }

    public void deleteWarehouseReceipt(Long id) {
        Optional<WarehouseReceipt> optionalWarehouseReceipt = warehouseReceiptRepository.findById(id);
        if (optionalWarehouseReceipt.isEmpty()) {
            throw new EntityNotFoundException("حواله ای با شناسه " + id + "یافت نشد.");
        }

        WarehouseReceipt warehouseReceipt = optionalWarehouseReceipt.get();

        if (warehouseReceipt.getInvoiceItem() != null) {
            throw new DatabaseIntegrityViolationException("امكان حذف حواله انبار وجود ندارد چون آيتم‌هاي فاكتور مرتبط دارد.");
        }
        if (warehouseReceipt.getReportItem() != null) {
            throw new DatabaseIntegrityViolationException("امکان حذف حواله وجود ندارد چون آیتم ‌های گزارش مرتبط دارد.");
        }
        warehouseReceiptRepository.deleteById(id);
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
    public void importWarehouseReceiptsFromExcel(MultipartFile file) throws IOException {
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);

            Map<String, WarehouseReceiptDto> map = new HashMap<>();

            for (Row row : sheet) {
                if (row.getRowNum() == 0) {
                    continue;
                }
                try {
                    int rowNum = row.getRowNum() + 1; // Adjust row number to be 1-based
                    String receiptNumber = String.valueOf((long) row.getCell(0).getNumericCellValue());

                    // Check if the invoice with this ID already exists
                    WarehouseReceiptDto receiptDto = map.get(receiptNumber);
                    if (receiptDto == null) {
                        receiptDto = new WarehouseReceiptDto();
                        receiptDto.setWarehouseReceiptNumber((long) row.getCell(0).getNumericCellValue());
                        receiptDto.setWarehouseReceiptDate(convertDate(row.getCell(1).getStringCellValue()));
                        receiptDto.setWarehouseReceiptDescription(row.getCell(2).getStringCellValue());
                        String customerCode = String.valueOf((long) row.getCell(3).getNumericCellValue());
                        Customer customer = customerRepository
                                .findByCustomerCode(customerCode)
                                .orElseThrow(() -> new EntityNotFoundException("مشتری با کد " + customerCode + "یافت نشد."));
                        receiptDto.setCustomerId(customer.getId());
                        receiptDto.setYearName((long) row.getCell(4).getNumericCellValue());
                        receiptDto.setWarehouseReceiptItems(new ArrayList<>());
                        map.put(receiptNumber, receiptDto);
                    }

                    WarehouseReceiptItemDto warehouseReceiptItemDto = new WarehouseReceiptItemDto();
                    warehouseReceiptItemDto.setQuantity((int) row.getCell(5).getNumericCellValue());
                    warehouseReceiptItemDto.setUnitPrice((long) row.getCell(6).getNumericCellValue());
                    String productCode = String.valueOf((long) row.getCell(7).getNumericCellValue());
                    Product product = productRepository
                            .findByProductCode(productCode)
                            .orElseThrow(() -> new EntityNotFoundException("محصولی با کد " + productCode + "یافت نشد."));
                    warehouseReceiptItemDto.setProductId(product.getId());
                    receiptDto.getWarehouseReceiptItems().add(warehouseReceiptItemDto);
                } catch (Exception e) {
                    // Catch exceptions that may occur while processing the row
                    int rowNum = row.getRowNum() + 1; // Adjust row number to be 1-based
                    throw new RowImportException(rowNum, e.getMessage());
                }

            }
            for (WarehouseReceiptDto i : map.values()) {
                warehouseReceiptRepository.save(warehouseReceiptMapper.toEntity(i));
            }
        }
    }


    public XSSFWorkbook generateWarehouseReceiptListExcel() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Warehouse Receipts");

        List<WarehouseReceiptDto> warehouseReceiptList = warehouseReceiptRepository.findAll().stream().map(warehouseReceiptMapper::toDto).toList();

        XSSFRow headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("ردیف حواله");
        headerRow.createCell(1).setCellValue("شناسه حواله");
        headerRow.createCell(2).setCellValue("تاریخ حواله");
        headerRow.createCell(3).setCellValue("تعداد");
        headerRow.createCell(4).setCellValue("مبلغ واحد(ریال)");
        headerRow.createCell(5).setCellValue("شناسه محصول");
        headerRow.createCell(6).setCellValue("کد محصول");

        int rowNum = 1;
        for (WarehouseReceiptDto receipt : warehouseReceiptList) {
            XSSFRow row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(receipt.getId());
            row.createCell(1).setCellValue(receipt.getWarehouseReceiptNumber());
            row.createCell(2).setCellValue(receipt.getWarehouseReceiptDate().toString());
//            row.createCell(3).setCellValue(receipt.getQuantity());
//            row.createCell(4).setCellValue(receipt.getUnitPrice());
//            row.createCell(5).setCellValue(receipt.getProductId());
//            row.createCell(6).setCellValue(receipt.getProductCode());
        }

        return workbook;
    }
}

