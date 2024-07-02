package com.armaninvestment.parsparandreporter.services;

import com.armaninvestment.parsparandreporter.dtos.*;
import com.armaninvestment.parsparandreporter.entities.*;
import com.armaninvestment.parsparandreporter.enums.SalesType;
import com.armaninvestment.parsparandreporter.exceptions.RowImportException;
import com.armaninvestment.parsparandreporter.exceptions.WarehouseReceiptAlreadyAssociatedException;
import com.armaninvestment.parsparandreporter.mappers.InvoiceListMapper;
import com.armaninvestment.parsparandreporter.mappers.InvoiceMapper;
import com.armaninvestment.parsparandreporter.repositories.*;
import com.github.eloyzone.jalalicalendar.DateConverter;
import com.github.eloyzone.jalalicalendar.JalaliDate;
import com.github.eloyzone.jalalicalendar.JalaliDateFormatter;
import jakarta.persistence.EntityManager;
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
    private final WarehouseInvoiceRepository warehouseInvoiceRepository;


    @Autowired
    public InvoiceService(InvoiceRepository invoiceRepository, InvoiceMapper invoiceMapper, InvoiceListMapper invoiceListMapper,
                          WarehouseReceiptRepository warehouseReceiptRepository,
                          CustomerRepository customerRepository, ProductRepository productRepository, ContractRepository contractRepository, YearRepository yearRepository, WarehouseInvoiceRepository warehouseInvoiceRepository) {
        this.invoiceRepository = invoiceRepository;
        this.invoiceMapper = invoiceMapper;
        this.invoiceListMapper = invoiceListMapper;
        this.warehouseReceiptRepository = warehouseReceiptRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.contractRepository = contractRepository;
        this.yearRepository = yearRepository;
        this.warehouseInvoiceRepository = warehouseInvoiceRepository;
    }

    public Long getMaxInvoiceNumber() {
        return invoiceRepository.findMaxInvoiceNumber();
    }

    @Transactional
    public InvoiceDto createInvoice(InvoiceDto invoiceDto) {
        Invoice invoice = invoiceMapper.toEntity(invoiceDto);
        Invoice saved = invoiceRepository.save(invoice);
        saved.getInvoiceItems().forEach(invoiceItem -> {
            Optional<WarehouseInvoice> optionalWarehouseInvoice = warehouseInvoiceRepository.findWarehouseInvoiceByReceiptId(invoiceItem.getWarehouseReceipt().getId());
            if (optionalWarehouseInvoice.isPresent()) {
                warehouseInvoiceRepository.updateInvoiceIdByReceiptId(invoiceItem.getInvoice().getId(), invoiceItem.getWarehouseReceipt().getId());
            }
        });
        return invoiceMapper.toDto(saved);
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


    private boolean isWareHouseReceiptDuplicate(Long warehouseReceiptId, Long currentInvoiceItemId) {
        return warehouseReceiptRepository.isDuplicateWarehouseReceipt(warehouseReceiptId, currentInvoiceItemId);
    }

    @Transactional
    public void updateInvoice(Long invoiceId, InvoiceDto invoiceDto) {
        if (!invoiceRepository.existsById(invoiceId))
            throw new EntityNotFoundException("فاکتور با مقدار " + invoiceId + " یافت نشد.");
        Year year = yearRepository.findByYearName(invoiceDto.getYearName()).orElseThrow(() -> new EntityNotFoundException("سال با مقدار " + invoiceDto.getYearName() + " یافت نشد."));
        if (!customerRepository.existsById(invoiceDto.getCustomerId()))
            throw new EntityNotFoundException("مشتری با شناسه " + invoiceDto.getCustomerId() + " یافت نشد.");

        invoiceRepository.updateInvoice(
                invoiceDto.getInvoiceNumber(),
                invoiceDto.getIssuedDate(),
                invoiceDto.getDueDate(),
                invoiceDto.getAdvancedPayment(),
                invoiceDto.getPerformanceBound(),
                invoiceDto.getInsuranceDeposit(),
                invoiceDto.getContractId(),
                invoiceDto.getSalesType().toString(),
                invoiceDto.getCustomerId(),
                invoiceDto.getInvoiceStatusId(),
                year.getId(),
                invoiceId
        );
        invoiceDto.getInvoiceItems().forEach(invoiceItem -> {
            Optional<WarehouseInvoice> optionalWarehouseInvoice = warehouseInvoiceRepository.findWarehouseInvoiceByReceiptId(invoiceItem.getWarehouseReceiptId());
            if (optionalWarehouseInvoice.isPresent()) {
                warehouseInvoiceRepository.updateInvoiceIdToNullByInvoiceId(invoiceItem.getInvoiceId());
            }
        });
        invoiceRepository.deleteInvoiceItems(invoiceId);

        for (InvoiceItemDto invoiceItemDto : invoiceDto.getInvoiceItems()) {
            Long receiptId = invoiceItemDto.getWarehouseReceiptId();
            if (!warehouseReceiptRepository.isWarehouseReceiptExistById(receiptId)) {
                throw new EntityNotFoundException("حواله ای با شناسه " + receiptId + " یافت نشد.");
            }
            if (isWareHouseReceiptDuplicate(receiptId, invoiceItemDto.getId())) {
                throw new WarehouseReceiptAlreadyAssociatedException("حواله " + receiptId + " با فاکتور دیگری در ارتباط می باشد.");
            }
            invoiceRepository.createInvoiceItem(
                    invoiceItemDto.getProductId(),
                    invoiceItemDto.getQuantity(),
                    invoiceItemDto.getUnitPrice(),
                    invoiceId,
                    invoiceItemDto.getWarehouseReceiptId()

            );
        }
        invoiceDto.getInvoiceItems().forEach(invoiceItem -> {
            Optional<WarehouseInvoice> optionalWarehouseInvoice = warehouseInvoiceRepository.findWarehouseInvoiceByReceiptId(invoiceItem.getWarehouseReceiptId());
            if (optionalWarehouseInvoice.isPresent()) {
                warehouseInvoiceRepository.updateInvoiceIdByReceiptId(invoiceItem.getInvoiceId(), invoiceItem.getWarehouseReceiptId());
            }
        });

    }

    private void createInvoiceItem(Long invoiceId, InvoiceItemDto invoiceItemDto, EntityManager em) {
        em.createNativeQuery(
                        "INSERT INTO invoice_item (product_id, quantity, unit_price, invoice_id, warehouse_receipt_id) " +
                                "VALUES (:productId, :quantity, :unitPrice, :invoiceId, :warehouseReceiptId)")
                .setParameter("productId", invoiceItemDto.getProductId())
                .setParameter("quantity", invoiceItemDto.getQuantity())
                .setParameter("unitPrice", invoiceItemDto.getUnitPrice())
                .setParameter("invoiceId", invoiceId)
                .setParameter("warehouseReceiptId", invoiceItemDto.getWarehouseReceiptId())
                .executeUpdate();
    }

    public void deleteInvoice(Long id) throws InterruptedIOException {
        Optional<Invoice> optionalInvoice = invoiceRepository.findById(id);
        if (optionalInvoice.isEmpty()) {
            throw new EntityNotFoundException("فاکتوری ای با شناسه " + id + "یافت نشد.");
        }
        if (optionalInvoice.get().getInvoiceItems() != null) {
            optionalInvoice.get().getInvoiceItems().forEach(invoiceItem -> {
                Optional<WarehouseInvoice> optionalWarehouseInvoice = warehouseInvoiceRepository.findWarehouseInvoiceByReceiptId(invoiceItem.getWarehouseReceipt().getId());
                if (optionalWarehouseInvoice.isPresent()) {
                    warehouseInvoiceRepository.updateInvoiceIdToNullByInvoiceId(invoiceItem.getInvoice().getId());
                }
            });
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

    public static String convertToPersianDate(LocalDate date) {
        DateConverter dateConverter = new DateConverter();
        JalaliDate jalaliDate = dateConverter.gregorianToJalali(date.getYear(), date.getMonth(), date.getDayOfMonth());
        return jalaliDate.format(new JalaliDateFormatter("yyyy/mm/dd", JalaliDateFormatter.FORMAT_IN_PERSIAN));
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

                try {
                    int rowNum = row.getRowNum() + 1; // Adjust row number to be 1-based
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
                            Long idByContractNumber = contractRepository.getContractIdByContractNumber(contractNumber);
                            if (idByContractNumber == null) {
                                throw new EntityNotFoundException("قرارداد با شماره قرارداد " + contractNumber + " یافت نشد.");
                            } else {
                                invoiceDto.setContractId(idByContractNumber);
                            }
                        } else {
                            invoiceDto.setContractId(null);
                        }
                        invoiceDto.setInvoiceStatusId((int) row.getCell(5).getNumericCellValue());
                        invoiceDto.setYearName((long) row.getCell(6).getNumericCellValue());
                        long customerCode = (long) row.getCell(7).getNumericCellValue();
                        Long customerId = customerRepository.getCustomerIdByCustomerCode(String.valueOf(customerCode));
                        if (customerId == null) {
                            throw new EntityNotFoundException("مشتری با کد " + customerCode + " یافت نشد.");
                        }
                        invoiceDto.setCustomerId(customerId);
                        invoiceDto.setAdvancedPayment((long) row.getCell(8).getNumericCellValue());
                        invoiceDto.setPerformanceBound((long) row.getCell(9).getNumericCellValue());
                        invoiceDto.setInsuranceDeposit((long) row.getCell(10).getNumericCellValue());
                        invoiceDto.setInvoiceItems(new LinkedHashSet<>());
                        invoiceMap.put(invoiceNumber, invoiceDto);
                    }

                    // Create and add invoiceDto items to the invoiceDto
                    InvoiceItemDto invoiceItemDto = new InvoiceItemDto();
                    String productCode = String.valueOf((long) row.getCell(11).getNumericCellValue());
                    Long productIdByProductCode = productRepository.getProductIdByProductCode(productCode);
                    if (productIdByProductCode == null) {
                        throw new EntityNotFoundException("محصولی با کد " + productCode + "یافت نشد.");
                    }
                    invoiceItemDto.setProductId(productIdByProductCode);
                    invoiceItemDto.setQuantity((int) row.getCell(12).getNumericCellValue());
                    invoiceItemDto.setUnitPrice((long) row.getCell(13).getNumericCellValue());
                    long receiptNumber = (long) row.getCell(14).getNumericCellValue();
                    LocalDate receiptDate = convertDate(row.getCell(15).getStringCellValue());
                    WarehouseReceipt receipt = warehouseReceiptRepository.findWarehouseReceiptByWarehouseReceiptNumberAndWarehouseReceiptDate(receiptNumber, receiptDate);
                    invoiceItemDto.setWarehouseReceiptId(receipt.getId());
                    invoiceDto.getInvoiceItems().add(invoiceItemDto);
                } catch (Exception e) {
                    // Catch exceptions that may occur while processing the row
                    int rowNum = row.getRowNum() + 1; // Adjust row number to be 1-based

                    throw new RowImportException(rowNum, e.getMessage());
                }
            }
            // Save invoices to the database
            for (InvoiceDto i : invoiceMap.values()) {
                invoiceRepository.save(invoiceMapper.toEntity(i));
            }

        }
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

    public List<InvoiceListRowDto> getInvoiceListByCustomerCodeAndYearName(String customerCode, Long yearName) {
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

        return invoiceRepository.mapToInvoicedListRowDto(customerCode, yearName);
    }

    public InvoiceDtoByQuery getInvoiceById(Long invoiceId) {
        List<Object[]> invoiceResult = invoiceRepository.getInvoiceById(invoiceId);
        List<Object[]> invoiceItemsResult = invoiceRepository.getInvoiceItemsByInvoiceId(invoiceId);

        InvoiceDtoByQuery invoiceDto = mapToInvoiceDto(invoiceResult);
        Set<InvoiceDtoByQuery.InvoiceItemDto> invoiceItems = mapToInvoiceItemDtoSet(invoiceItemsResult);

        if (invoiceDto != null) {
            invoiceDto.setInvoiceItems(invoiceItems);
        }

        return invoiceDto;
    }

    private InvoiceDtoByQuery mapToInvoiceDto(List<Object[]> result) {
        if (result.isEmpty()) {
            return null;
        }

        Object[] row = result.get(0);

        Long invoiceId = (Long) row[0];
        Long invoiceNumber = (Long) row[1];
        LocalDate issuedDate = ((java.sql.Date) row[2]).toLocalDate();
        LocalDate dueDate = ((java.sql.Date) row[3]).toLocalDate();
        Long contractId = (Long) row[4];
        String salesType = (String) row[5];
        Long customerId = (Long) row[6];
        Integer invoiceStatusId = (Integer) row[7];
        Long yearName = (Long) row[8];
        Long advancedPayment = (Long) row[9];
        Long insuranceDeposit = (Long) row[10];
        Long performanceBound = (Long) row[11];


        return new InvoiceDtoByQuery(invoiceId, invoiceNumber, issuedDate, dueDate, null,
                contractId, SalesType.valueOf(salesType), customerId, invoiceStatusId, yearName, advancedPayment, insuranceDeposit, performanceBound);
    }

    private Set<InvoiceDtoByQuery.InvoiceItemDto> mapToInvoiceItemDtoSet(List<Object[]> result) {
        return result.stream()
                .map(this::mapToInvoiceItemDto)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private InvoiceDtoByQuery.InvoiceItemDto mapToInvoiceItemDto(Object[] row) {
        Long itemId = (Long) row[0];
        Long productId = (Long) row[1];
        Integer quantity = (Integer) row[2];
        Long unitPrice = (Long) row[3];
        Long warehouseReceiptId = (Long) row[4];
        Long invoiceId = (Long) row[5];

        return new InvoiceDtoByQuery.InvoiceItemDto(itemId, productId, quantity, unitPrice, warehouseReceiptId, invoiceId);
    }


}

