package com.armaninvestment.parsparandreporter.services;

import com.armaninvestment.parsparandreporter.entities.Payment;
import com.armaninvestment.parsparandreporter.repositories.PaymentRepository;
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
import java.time.LocalDate;
import java.time.ZoneId;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;

    @Autowired
    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public void importPaymentsFromExcel(MultipartFile file) throws IOException {
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0); // Assuming the data is in the first sheet

            for (Row currentRow : sheet) {
                if (currentRow.getRowNum() == 0) {
                    // Skip the header row
                    continue;
                }

                // Extract payment data from Excel columns, adjust indices as needed
                String description = currentRow.getCell(0).getStringCellValue();
                LocalDate date = currentRow.getCell(1).getDateCellValue().toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
                Double amount = currentRow.getCell(2).getNumericCellValue();
                // Extract other payment fields...

                // Create and save a new Payment entity
                Payment payment = new Payment();
                payment.setDescription(description);
                payment.setDate(date);
                payment.setAmount(amount);
                // Set other payment fields...
                paymentRepository.save(payment);
            }
        }
    }
}

