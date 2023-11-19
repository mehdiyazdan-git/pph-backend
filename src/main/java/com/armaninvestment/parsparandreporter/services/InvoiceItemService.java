package com.armaninvestment.parsparandreporter.services;

import com.armaninvestment.parsparandreporter.entities.InvoiceItem;
import com.armaninvestment.parsparandreporter.repositories.InvoiceItemRepository;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

@Service
public class InvoiceItemService {


    private final InvoiceItemRepository invoiceItemRepository;

    @Autowired
    public InvoiceItemService(InvoiceItemRepository invoiceItemRepository) {
        this.invoiceItemRepository = invoiceItemRepository;
    }


    // Other CRUD methods...

    public void importInvoiceItemsFromExcel(MultipartFile file) throws IOException {
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0); // Assuming the data is in the first sheet

            Iterator<Row> iterator = sheet.iterator();
            while (iterator.hasNext()) {
                Row currentRow = iterator.next();
                if (currentRow.getRowNum() == 0) {
                    // Skip the header row
                    continue;
                }

                // Extract invoice item data from Excel columns, adjust indices as needed
                Integer quantity = (int) currentRow.getCell(0).getNumericCellValue();
                Long unitPrice = (long) currentRow.getCell(1).getNumericCellValue();
                // Extract other invoice item fields...

                // Create and save a new InvoiceItem entity
                InvoiceItem invoiceItem = new InvoiceItem();
                invoiceItem.setQuantity(quantity);
                invoiceItem.setUnitPrice(unitPrice);
                // Set other invoice item fields...
                invoiceItemRepository.save(invoiceItem);
            }
        }
    }
}

