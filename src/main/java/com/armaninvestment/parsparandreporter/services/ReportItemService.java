package com.armaninvestment.parsparandreporter.services;

import com.armaninvestment.parsparandreporter.entities.ReportItem;
import com.armaninvestment.parsparandreporter.repositories.ReportItemRepository;
import jakarta.transaction.Transactional;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@Service
public class ReportItemService {
    private final ReportItemRepository reportItemRepository;

    @Autowired
    public ReportItemService(ReportItemRepository reportItemRepository) {
        this.reportItemRepository = reportItemRepository;
    }

    @Transactional
    public void importReportItemsFromExcel(MultipartFile file) throws IOException {
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0); // Assuming the data is in the first sheet

            for (Row currentRow : sheet) {
                if (currentRow.getRowNum() == 0) {
                    // Skip the header row
                    continue;
                }

                // Extract report item data from Excel columns, adjust indices as needed
                Long inventoryPaper = (long) currentRow.getCell(0).getNumericCellValue();
                String productCode = currentRow.getCell(1).getStringCellValue();
                Long unitPrice = (long) currentRow.getCell(2).getNumericCellValue();
                Integer quantity = (int) currentRow.getCell(3).getNumericCellValue();
                // Extract other report item fields...

                // Create and save a new ReportItem entity
                ReportItem reportItem = new ReportItem();
                reportItem.setUnitPrice(unitPrice);
                reportItem.setQuantity(quantity);
                // Set other report item fields...
                reportItemRepository.save(reportItem);
            }
        }
    }
}

