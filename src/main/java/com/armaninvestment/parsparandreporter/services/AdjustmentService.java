package com.armaninvestment.parsparandreporter.services;

import com.armaninvestment.parsparandreporter.dtos.AdjustmentByQuery;
import com.armaninvestment.parsparandreporter.dtos.AdjustmentDto;
import com.armaninvestment.parsparandreporter.entities.Adjustment;
import com.armaninvestment.parsparandreporter.entities.Invoice;
import com.armaninvestment.parsparandreporter.enums.AdjustmentType;
import com.armaninvestment.parsparandreporter.mappers.AdjustmentMapper;
import com.armaninvestment.parsparandreporter.repositories.AdjustmentRepository;
import jakarta.persistence.EntityNotFoundException;
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
import java.sql.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AdjustmentService {

    private final AdjustmentRepository adjustmentRepository;
    private final AdjustmentMapper adjustmentMapper;

    @Autowired
    public AdjustmentService(AdjustmentRepository adjustmentRepository, AdjustmentMapper adjustmentMapper) {
        this.adjustmentRepository = adjustmentRepository;
        this.adjustmentMapper = adjustmentMapper;
    }

    public void createAdjustment(AdjustmentDto adjustmentDto) {
        adjustmentRepository.createAdjustment(
                adjustmentDto.getAdjustmentNumber(),
                adjustmentDto.getAdjustmentDate(),
                adjustmentDto.getDescription(),
                adjustmentDto.getUnitPrice(),
                adjustmentDto.getQuantity(),
                adjustmentDto.getAdjustmentType().toString(), // Assuming AdjustmentType is an enum
                adjustmentDto.getInvoiceId()
        );
    }

    public Long getTotalAdjustmentAmountByYearNameAndMonthAndCustomerId(Long customerId, Long yearName, Integer month) {
        return adjustmentRepository.getTotalAdjustmentAmountByYearNameAndMonthAndCustomerId(customerId, yearName, month);
    }


    public List<AdjustmentByQuery> getAllAdjustments() {
        return adjustmentRepository.getAdjustments().stream().map(obj -> {
            AdjustmentByQuery adjustment = new AdjustmentByQuery();
            adjustment.setAdjustmentId((Long) obj[0]);
            adjustment.setAdjustmentType(this.getPersianCaption((String) obj[1]));
            adjustment.setAdjustmentDescription((String) obj[2]);
            adjustment.setAdjustmentQuantity((Long) obj[3]);
            adjustment.setAdjustmentUnitPrice((Double) obj[4]);
            adjustment.setAmount((Double) obj[5]);
            adjustment.setInvoiceId((Long) obj[6]);
            adjustment.setAdjustmentDate(((Date) obj[7]).toLocalDate());
            adjustment.setAdjustmentNumber((Long) obj[8]);
            adjustment.setInvoiceNumber((Long) obj[9]);
            return adjustment;
        }).collect(Collectors.toList());
    }

    private String getPersianCaption(String type) {
        if (Objects.equals(type, "POSITIVE")) {
            return "تعدیل مثبت";
        } else if (Objects.equals(type, "NEGATIVE")) {
            return "تعدیل منفی";
        }
        return type;
    }


    public List<AdjustmentDto> getAdjustmentById(Long adjustmentId) {
        return adjustmentRepository.getAdjustmentById(adjustmentId).stream().map(obj -> {
            AdjustmentDto adjustment = new AdjustmentDto();
            adjustment.setId((Long) obj[0]);
            adjustment.setDescription((String) obj[1]);
            adjustment.setAdjustmentNumber((Long) obj[2]);
            adjustment.setAdjustmentDate(((Date) obj[3]).toLocalDate());
            adjustment.setUnitPrice((Double) obj[4]);
            adjustment.setQuantity((Long) obj[5]);
            String adjustmentTypeStr = (String) obj[6];
            AdjustmentType adjustmentTypeEnum = AdjustmentType.valueOf(adjustmentTypeStr);
            adjustment.setAdjustmentType(adjustmentTypeEnum);
            adjustment.setInvoiceId((Long) obj[7]);
            return adjustment;
        }).collect(Collectors.toList());
    }


    public void updateAdjustment(Long adjustmentId, AdjustmentDto adjustmentDto) {
        if (!adjustmentRepository.existsById(adjustmentId))
            throw new EntityNotFoundException("سند برگشت از فروش با شناسه " + adjustmentId + " یافت نشد.");
        adjustmentRepository.updateAdjustment(
                adjustmentDto.getAdjustmentNumber(),
                adjustmentDto.getAdjustmentDate(),
                adjustmentDto.getDescription(),
                adjustmentDto.getUnitPrice(),
                adjustmentDto.getQuantity(),
                adjustmentDto.getAdjustmentType().toString(),
                adjustmentDto.getInvoiceId(),
                adjustmentId
        );
    }

    public void deleteAdjustment(Long adjustmentId) {
        Optional<Adjustment> optionalAdjustment = adjustmentRepository.findById(adjustmentId);
        if (optionalAdjustment.isEmpty()) {
            throw new EntityNotFoundException("سند تعدیل  با شناسه " + adjustmentId + "یافت نشد.");
        }
        Adjustment adjustment = optionalAdjustment.get();

        adjustmentRepository.delete(adjustment);
    }

    @Transactional
    public void importAdjustmentsFromExcel(MultipartFile file) throws IOException {
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);

            for (Row currentRow : sheet) {
                if (currentRow.getRowNum() == 0) {
                    continue;
                }
                Long id = (long) currentRow.getCell(0).getNumericCellValue();
                String description = currentRow.getCell(1).getStringCellValue();
                Double unitPrice = currentRow.getCell(2).getNumericCellValue();
                Long quantity = (long) currentRow.getCell(3).getNumericCellValue();
                String adjustmentType = currentRow.getCell(4).getStringCellValue();
                Long invoiceId = (long) currentRow.getCell(5).getNumericCellValue();


                Adjustment adjustment = new Adjustment();
                adjustment.setId(id);
                adjustment.setDescription(description);
                adjustment.setUnitPrice(unitPrice);
                adjustment.setQuantity(quantity);
                adjustment.setAdjustmentType(AdjustmentType.valueOf(adjustmentType));
                adjustment.setInvoice(new Invoice(invoiceId));
                adjustmentRepository.save(adjustment);
            }
        }
    }
}
