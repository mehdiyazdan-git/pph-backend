package com.armaninvestment.parsparandreporter.services;

import com.armaninvestment.parsparandreporter.dtos.ReportDto;
import com.armaninvestment.parsparandreporter.dtos.ReportItemDto;
import com.armaninvestment.parsparandreporter.dtos.ReportWithSubtotalDTO;
import com.armaninvestment.parsparandreporter.dtos.SalesByYearGroupByMonth;
import com.armaninvestment.parsparandreporter.entities.*;
import com.armaninvestment.parsparandreporter.exceptions.RowImportException;
import com.armaninvestment.parsparandreporter.exceptions.WarehouseReceiptNotFoundException;
import com.armaninvestment.parsparandreporter.mappers.ReportMapper;
import com.armaninvestment.parsparandreporter.repositories.CustomerRepository;
import com.armaninvestment.parsparandreporter.repositories.ReportRepository;
import com.armaninvestment.parsparandreporter.repositories.WarehouseReceiptRepository;
import com.armaninvestment.parsparandreporter.repositories.YearRepository;
import com.github.eloyzone.jalalicalendar.DateConverter;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static com.armaninvestment.parsparandreporter.services.InvoiceService.convertToPersianDate;

@Service
public class ReportService {
    private final ReportRepository reportRepository;
    private final ReportMapper reportMapper;
    private final WarehouseReceiptRepository warehouseReceiptRepository;
    private final CustomerRepository customerRepository;
    private final YearRepository yearRepository;


    @Autowired
    public ReportService(ReportRepository reportRepository, ReportMapper reportMapper, WarehouseReceiptRepository warehouseReceiptRepository, CustomerRepository customerRepository, YearRepository yearRepository) {
        this.reportRepository = reportRepository;
        this.reportMapper = reportMapper;
        this.warehouseReceiptRepository = warehouseReceiptRepository;
        this.customerRepository = customerRepository;
        this.yearRepository = yearRepository;
    }

    public List<ReportWithSubtotalDTO> getAllReportsByYearId(Long yearName) {
        Optional<Year> optionalYear = yearRepository.findByYearName(yearName);
        if (optionalYear.isEmpty()) throw new EntityNotFoundException("سال با نام " + yearName + " یافت نشد.");
        List<Object[]> objects = reportRepository.getAllReportsByYearId(optionalYear.get().getId());
        List<ReportWithSubtotalDTO> list = new ArrayList<>();
        for (Object[] obj : objects) {
            ReportWithSubtotalDTO dto = new ReportWithSubtotalDTO();
            dto.setId((Long) obj[0]);
            Date sqlDate = (Date) obj[1];
            dto.setDate(LocalDate.parse(sqlDate.toString()));
            dto.setTotalAmount((BigDecimal) obj[2]);
            dto.setTotalCount((Long) obj[3]);
            list.add(dto);
        }
        return list;
    }

    @Transactional
    public void updateReport(Long reportId, ReportDto reportDto) {
        if (!reportRepository.existsById(reportId))
            throw new EntityNotFoundException("گزارش با شناسه " + reportId + " یافت نشد.");

        reportRepository.updateReport(
                reportDto.getExplanation(),
                reportDto.getDate(),
                reportId
        );

        reportRepository.deleteReportItems(reportId);

        for (ReportItemDto reportItemDto : reportDto.getReportItems()) {
            Long receiptId = reportItemDto.getWarehouseReceiptId();
            if (!warehouseReceiptRepository.isWarehouseReceiptExistById(receiptId)) {
                throw new EntityNotFoundException("حواله ای با شناسه " + receiptId + " یافت نشد.");
            }
            reportRepository.createReportItem(
                    reportItemDto.getCustomerId(),
                    reportItemDto.getQuantity(),
                    reportItemDto.getUnitPrice(),
                    reportId,
                    reportItemDto.getWarehouseReceiptId()

            );
        }
    }

    public Page<ReportWithSubtotalDTO> getReportsWithSubtotals(
            Integer pageNo,
            Integer pageSize,
            String sortBy,
            LocalDate startDate,
            LocalDate endDate
    ) {
        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).descending());
        Page<Report> pagedResult;

        if (startDate != null && endDate != null) {
            pagedResult = reportRepository.findByDateBetweenOrderByDateDesc(startDate, endDate, paging);
        } else if (startDate != null) {
            pagedResult = reportRepository.findByDateAfterOrderByDateDesc(startDate, paging);
        } else if (endDate != null) {
            pagedResult = reportRepository.findByDateBeforeOrderByDateDesc(endDate, paging);
        } else {
            pagedResult = reportRepository.findAll(paging);
        }

        return pagedResult.map(report -> {
            ReportWithSubtotalDTO reportDTO = new ReportWithSubtotalDTO();
            reportDTO.setId(report.getId());
            reportDTO.setDate(report.getDate());

            List<ReportItem> reportItems = report.getReportItems();
            long totalCount = 0;
            long totalAmount = 0;

            for (ReportItem reportItem : reportItems) {
                totalCount += reportItem.getQuantity();
                totalAmount += reportItem.getQuantity() * reportItem.getUnitPrice();
            }

            reportDTO.setTotalCount(totalCount);
            reportDTO.setTotalAmount(BigDecimal.valueOf(totalAmount));

            return reportDTO;
        });
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

    public void importReportsFromExcel(MultipartFile file) throws IOException {
        Map<String, ReportDto> reportMap = new HashMap<>();
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() == 0) {
                    continue;
                }
                String reportId = row.getCell(0).getStringCellValue();
                ReportDto reportDto = reportMap.get(reportId);
                try {
                    int rowNum = row.getRowNum() + 1; // Adjust row number to be 1-based

                    if (reportDto == null) {
                        reportDto = new ReportDto();
                        reportDto.setExplanation(row.getCell(1).getStringCellValue());
                        reportDto.setDate(convertDate(row.getCell(2).getStringCellValue()));
                        reportDto.setYearName((long) row.getCell(3).getNumericCellValue());
                        reportDto.setReportItems(new ArrayList<>());
                        reportMap.put(reportId, reportDto);
                    }
                    ReportItemDto reportItem = new ReportItemDto();
                    reportItem.setUnitPrice((long) row.getCell(4).getNumericCellValue());
                    reportItem.setQuantity((int) row.getCell(5).getNumericCellValue());
                    long customerCode = (long) row.getCell(6).getNumericCellValue();
                    Customer customer = customerRepository.findByCustomerCode(String.valueOf(customerCode)).orElseThrow(() -> new EntityNotFoundException("مشتری با کد " + customerCode + " یافت نشد."));
                    reportItem.setCustomerId(customer.getId());
                    long receiptNumber = (long) row.getCell(7).getNumericCellValue();
                    LocalDate receiptDate = convertDate(row.getCell(8).getStringCellValue());
                    Long receiptIdByWarehouseReceiptNumberAndDate = warehouseReceiptRepository.getWarehouseReceiptIdByWarehouseReceiptNumberAndDate(receiptNumber, receiptDate);
                    if (receiptIdByWarehouseReceiptNumberAndDate == null) {
                        throw new WarehouseReceiptNotFoundException("حواله با شناسه " + receiptNumber + " و تاریخ " + convertToPersianDate(receiptDate) + " یافت نشد.");
                    } else {
                        reportItem.setWarehouseReceiptId(receiptIdByWarehouseReceiptNumberAndDate);
                    }
                    reportDto.getReportItems().add(reportItem);
                    reportMap.put(reportId, reportDto);
                } catch (Exception e) {
                    int rowNum = row.getRowNum() + 1;
                    throw new RowImportException(rowNum, e.getMessage());
                }
            }
        }
        for (ReportDto reportDto : reportMap.values()) {
            reportDto.setYearName(3L);
            reportRepository.save(reportMapper.toEntity(reportDto));
        }
    }

    public XSSFWorkbook generateReportsToExcel(List<ReportDto> reports) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Reports");

        String[] headers = {"ID", "Explanation", "Date", "Inventory Paper", "Product Code", "Unit Price", "Quantity", "Customer ID"};
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
        for (ReportDto report : reports) {
            for (ReportItemDto reportItem : report.getReportItems()) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(report.getId());
                row.createCell(1).setCellValue(report.getExplanation());
                row.createCell(2).setCellValue(report.getDate().toString());
                row.createCell(3).setCellValue(reportItem.getUnitPrice());
                row.createCell(4).setCellValue(reportItem.getQuantity());
                row.createCell(5).setCellValue(reportItem.getCustomerId());
            }
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        return workbook;
    }

    public void deleteReport(Long id) {
        if (!reportRepository.existsById(id)) {
            throw new EntityNotFoundException("گزارش با شناسه " + id + "یافت نشد.");
        }
        reportRepository.deleteById(id);
    }

    public List<SalesByYearGroupByMonth> findSalesByYearGroupByMonth(Short yearName, String productType) {
        List<Object[]> results = reportRepository.getSalesByYearGroupByMonth(yearName, productType);
        List<SalesByYearGroupByMonth> list = new ArrayList<>();

        // Create a map to store the results by month number
        Map<Short, SalesByYearGroupByMonth> resultMap = new HashMap<>();

        for (Object[] result : results) {
            resultMap.put((Short) result[0], new SalesByYearGroupByMonth(
                    (Short) result[0],     // month number
                    (String) result[1],      // month name
                    (BigDecimal) result[2],  // total amount
                    (Long) result[3]         // total quantity
            ));
        }

        // Iterate through all 12 months and add them to the list, using the resultMap
        for (short monthNumber = 1; monthNumber <= 12; monthNumber++) {
            SalesByYearGroupByMonth monthData = resultMap.get(monthNumber);

            if (monthData == null) {
                // Month has no result, create an item with zero values
                list.add(new SalesByYearGroupByMonth(
                        monthNumber,
                        getMonthName(monthNumber), // You can implement this method to get the month name
                        BigDecimal.ZERO,
                        0L
                ));
            } else {
                list.add(monthData);
            }
        }

        return list;
    }

    private String getMonthName(int monthNumber) {
        // Define an array of Persian month names (replace with actual month names)
        String[] persianMonthNames = {
                "فروردین",
                "اردیبهشت",
                "خرداد",
                "تیر",
                "مرداد",
                "شهریور",
                "مهر",
                "آبان",
                "آذر",
                "دی",
                "بهمن",
                "اسفند"
        };

        // Check if the provided monthNumber is within a valid range (1 to 12)
        if (monthNumber >= 1 && monthNumber <= 12) {
            // Subtract 1 from the monthNumber to match the array index
            return persianMonthNames[monthNumber - 1];
        } else {
            return "Invalid Month";
        }
    }

    public ReportDtoByQuery getReportById(Long reportId) {
        List<Object[]> reportResult = reportRepository.getReportById(reportId);
        List<Object[]> reportItemsResult = reportRepository.getReportItemsByReportId(reportId);

        ReportDtoByQuery reportDto = mapToReportDto(reportResult);
        List<ReportDtoByQuery.ReportItemDto> reportItems = mapToReportItemDtoList(reportItemsResult);

        if (reportDto != null) {
            reportDto.setReportItems(reportItems);
        }

        return reportDto;
    }

    private ReportDtoByQuery mapToReportDto(List<Object[]> result) {
        if (result.isEmpty()) {
            return null;
        }

        Object[] row = result.get(0);

        Long reportId = (Long) row[0];
        String explanation = (String) row[1];
        LocalDate date = ((java.sql.Date) row[2]).toLocalDate();
        Long yearId = (Long) row[3];

        return new ReportDtoByQuery(reportId, explanation, date, null, yearId);
    }

    private List<ReportDtoByQuery.ReportItemDto> mapToReportItemDtoList(List<Object[]> result) {
        return result.stream()
                .map(this::mapToReportItemDto)
                .collect(Collectors.toList());
    }

    private ReportDtoByQuery.ReportItemDto mapToReportItemDto(Object[] row) {
        Long itemId = (Long) row[0];
        Integer quantity = (Integer) row[1];
        Long unitPrice = (Long) row[2];
        Long customerId = (Long) row[3];
        Long warehouseReceiptId = (Long) row[4];

        return new ReportDtoByQuery.ReportItemDto(itemId, unitPrice, quantity, customerId, warehouseReceiptId);
    }

}

