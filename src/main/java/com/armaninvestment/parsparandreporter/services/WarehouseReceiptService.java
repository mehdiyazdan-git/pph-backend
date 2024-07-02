package com.armaninvestment.parsparandreporter.services;

import com.armaninvestment.parsparandreporter.dtos.NotInvoiced;
import com.armaninvestment.parsparandreporter.dtos.WarehouseReceiptDto;
import com.armaninvestment.parsparandreporter.dtos.WarehouseReceiptItemDto;
import com.armaninvestment.parsparandreporter.dtos.list.WarehouseReceiptListDto;
import com.armaninvestment.parsparandreporter.entities.*;
import com.armaninvestment.parsparandreporter.exceptions.DatabaseIntegrityViolationException;
import com.armaninvestment.parsparandreporter.exceptions.RowImportException;
import com.armaninvestment.parsparandreporter.mappers.WarehouseInvoiceMapper;
import com.armaninvestment.parsparandreporter.mappers.WarehouseReceiptMapper;
import com.armaninvestment.parsparandreporter.repositories.*;
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
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class WarehouseReceiptService {

    private final WarehouseReceiptRepository warehouseReceiptRepository;
    private final WarehouseReceiptMapper warehouseReceiptMapper;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final YearRepository yearRepository;

    private final WarehouseInvoiceRepository warehouseInvoiceRepository;
    private final WarehouseInvoiceMapper warehouseInvoiceMapper;

    private final ReportItemRepository reportItemRepository;

    private final InvoiceItemRepository invoiceItemRepository;

    @Autowired
    public WarehouseReceiptService(WarehouseReceiptRepository warehouseReceiptRepository,
                                   WarehouseReceiptMapper warehouseReceiptMapper,
                                   ProductRepository productRepository,
                                   CustomerRepository customerRepository,
                                   YearRepository yearRepository, WarehouseInvoiceRepository warehouseInvoiceRepository, WarehouseInvoiceMapper warehouseInvoiceMapper, ReportItemRepository reportItemRepository, InvoiceItemRepository invoiceItemRepository) {
        this.warehouseReceiptRepository = warehouseReceiptRepository;
        this.warehouseReceiptMapper = warehouseReceiptMapper;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.yearRepository = yearRepository;
        this.warehouseInvoiceRepository = warehouseInvoiceRepository;
        this.warehouseInvoiceMapper = warehouseInvoiceMapper;
        this.reportItemRepository = reportItemRepository;
        this.invoiceItemRepository = invoiceItemRepository;
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


    public List<WarehouseReceiptListDto> getWarehouseReceiptList(Long yearName) {
        Optional<Year> optionalYear = yearRepository.findByYearName(yearName);
        if (optionalYear.isEmpty()) {
            throw new EntityNotFoundException("سال با شناسه " + yearName + " یافت نشد.");
        }
        List<Object[]> list = warehouseReceiptRepository.getAllWarehouseReceiptsByYearName(yearName);
        if (list.isEmpty()) {
            return null;
        }
        return list.stream().map(obj -> {
            WarehouseReceiptListDto dto = new WarehouseReceiptListDto();
            dto.setId((Long) obj[0]);
            dto.setWarehouseReceiptNumber((Long) obj[1]);
            dto.setWarehouseReceiptDate((String) obj[2]);
            dto.setWarehouseReceiptDescription((String) obj[3]);
            dto.setCustomerName((String) obj[4]);
            dto.setInvoiceNumber((Long) obj[5]);
            dto.setReportDate((String) obj[6]);
            dto.setTotalAmount((BigDecimal) obj[7]);
            dto.setTotalQuantity((Long) obj[8]);
            return dto;
        }).collect(Collectors.toList());
    }


    public WarehouseReceiptDtoByQuery getWarehouseReceiptById(Long warehouseReceiptId) {
        List<Object[]> warehouseReceiptData = warehouseReceiptRepository.getWarehouseReceiptById(warehouseReceiptId);

        if (warehouseReceiptData.isEmpty()) {
            throw new EntityNotFoundException("حواله با شناسه " + warehouseReceiptId + " یافت نشد.");
        }
        Object[] warehouseReceiptArray = warehouseReceiptData.get(0);
        WarehouseReceiptDtoByQuery resultDto = new WarehouseReceiptDtoByQuery();
        resultDto.setId((Long) warehouseReceiptArray[0]);
        resultDto.setWarehouseReceiptNumber((Long) warehouseReceiptArray[1]);
        resultDto.setWarehouseReceiptDate(((Date) warehouseReceiptArray[2]).toLocalDate());
        resultDto.setWarehouseReceiptDescription((String) warehouseReceiptArray[3]);
        resultDto.setCustomerId((Long) warehouseReceiptArray[4]);
        resultDto.setInvoiceNumber((Long) warehouseReceiptArray[5]);
        if (warehouseReceiptArray[6] != null) {
            resultDto.setReportDate(((Date) warehouseReceiptArray[6]).toLocalDate());
        } else {
            resultDto.setReportDate(null);
        }
        resultDto.setYearName((Long) warehouseReceiptArray[7]);

        // Populate WarehouseReceiptItemDtos
        List<WarehouseReceiptDtoByQuery.WarehouseReceiptItemDto> warehouseReceiptItemDtos = new ArrayList<>();
        List<Object[]> warehouseReceiptItemsData = warehouseReceiptRepository.getWarehouseReceiptItemsById(warehouseReceiptId);
        for (Object[] itemArray : warehouseReceiptItemsData) {
            WarehouseReceiptDtoByQuery.WarehouseReceiptItemDto item = new WarehouseReceiptDtoByQuery.WarehouseReceiptItemDto();
            item.setId((Long) itemArray[0]);
            item.setUnitPrice((Long) itemArray[1]);
            item.setQuantity((Integer) itemArray[2]);
            item.setProductId((Long) itemArray[3]);
            warehouseReceiptItemDtos.add(item);
        }

        resultDto.setWarehouseReceiptItems(warehouseReceiptItemDtos);

        return resultDto;
    }

    public static String convertToPersianDigits(Long number) {
        String englishDigits = "0123456789";
        String persianDigits = "۰۱۲۳۴۵۶۷۸۹";

        String numberStr = number.toString();
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < numberStr.length(); i++) {
            char ch = numberStr.charAt(i);
            int index = englishDigits.indexOf(ch);
            if (index >= 0) {
                result.append(persianDigits.charAt(index));
            } else {
                result.append(ch);
            }
        }

        return result.toString();
    }


    public WarehouseReceiptDto createWarehouseReceipt(WarehouseReceiptDto warehouseReceiptDto) {
        WarehouseReceipt warehouseReceipt = warehouseReceiptMapper.toEntity(warehouseReceiptDto);
        WarehouseReceipt savedWarehouseReceipt = warehouseReceiptRepository.save(warehouseReceipt);
        warehouseInvoiceRepository.save(new WarehouseInvoice(savedWarehouseReceipt.getId()));
        return warehouseReceiptMapper.toDto(savedWarehouseReceipt);
    }
    public void deleteWarehouseReceipt(Long id) {
        if (!warehouseReceiptRepository.existsById(id)) {
            throw new EntityNotFoundException("حواله ای با شناسه " + id + "یافت نشد.");
        }
        if (invoiceItemRepository.existsAllByWarehouseReceipt(new WarehouseReceipt(id))) {
            throw new DatabaseIntegrityViolationException("امكان حذف حواله انبار وجود ندارد چون آيتم‌هاي فاكتور مرتبط دارد.");
        }
        if (reportItemRepository.existsAllByWarehouseReceipt(new WarehouseReceipt(id))) {
            throw new DatabaseIntegrityViolationException("امکان حذف حواله وجود ندارد چون آیتم ‌های گزارش مرتبط دارد.");
        }
        Optional<WarehouseInvoice> optionalWarehouseInvoice = warehouseInvoiceRepository.findWarehouseInvoiceByReceiptId(id);
        if (optionalWarehouseInvoice.isEmpty()) {
            warehouseReceiptRepository.deleteById(id);
        } else {
            warehouseInvoiceRepository.delete(optionalWarehouseInvoice.get());
            warehouseReceiptRepository.deleteById(id);
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
    public void updateWarehouseReceipt(Long receiptId, WarehouseReceiptDto warehouseReceiptDto) {
        if (!warehouseReceiptRepository.existsById(receiptId))
            throw new EntityNotFoundException("حواله با مقدار " + receiptId + " یافت نشد.");
        Year year = yearRepository.findByYearName(warehouseReceiptDto.getYearName()).orElseThrow(() -> new EntityNotFoundException("سال با مقدار " + warehouseReceiptDto.getYearName() + " یافت نشد."));
        if (!customerRepository.checkCustomerExistsById(warehouseReceiptDto.getCustomerId()))
            throw new EntityNotFoundException("مشتری با شناسه " + warehouseReceiptDto.getCustomerId() + " یافت نشد.");

        warehouseReceiptRepository.updateWareHouseReceiptById(
                warehouseReceiptDto.getWarehouseReceiptDate(),
                warehouseReceiptDto.getWarehouseReceiptDescription(),
                warehouseReceiptDto.getWarehouseReceiptNumber(),
                warehouseReceiptDto.getCustomerId(),
                year.getId(),
                receiptId
        );

        warehouseReceiptRepository.deleteWareHouseReceiptItems(receiptId);

        for (WarehouseReceiptItemDto warehouseReceiptItemDto : warehouseReceiptDto.getWarehouseReceiptItems()) {
            Long productId = warehouseReceiptItemDto.getProductId();
            if (!productRepository.existsById(productId)) {
                throw new EntityNotFoundException("محصول ای با شناسه " + productId + " یافت نشد.");
            }
            warehouseReceiptRepository.insertWareHouseReceiptItem(
                    warehouseReceiptItemDto.getProductId(),
                    warehouseReceiptItemDto.getQuantity(),
                    warehouseReceiptItemDto.getUnitPrice(),
                    receiptId

            );
        }
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
                WarehouseReceipt saved = warehouseReceiptRepository.save(warehouseReceiptMapper.toEntity(i));
                warehouseInvoiceRepository.save(new WarehouseInvoice(saved.getId()));
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

