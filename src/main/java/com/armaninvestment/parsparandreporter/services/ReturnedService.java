package com.armaninvestment.parsparandreporter.services;


import com.armaninvestment.parsparandreporter.dtos.ReturnedByQuery;
import com.armaninvestment.parsparandreporter.dtos.ReturnedDto;
import com.armaninvestment.parsparandreporter.entities.Customer;
import com.armaninvestment.parsparandreporter.entities.Returned;
import com.armaninvestment.parsparandreporter.repositories.ReturnedRepository;
import com.github.eloyzone.jalalicalendar.DateConverter;
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
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReturnedService {

    private final ReturnedRepository returnedRepository;

    @Autowired
    public ReturnedService(ReturnedRepository returnedRepository) {
        this.returnedRepository = returnedRepository;
    }


    public void createReturned(ReturnedDto returnedDto) {
        try {
            returnedRepository.createReturned(
                    returnedDto.getReturnedNumber(),
                    returnedDto.getReturnedDate(),
                    returnedDto.getReturnedDescription(),
                    returnedDto.getQuantity(),
                    returnedDto.getUnitPrice(),
                    returnedDto.getCustomerId()
            );
        } catch (RuntimeException e) {
            throw new RuntimeException();
        }
    }


    public List<ReturnedByQuery> getAllReturned() {
        return returnedRepository.getAllReturned().stream().map(obj -> {
            ReturnedByQuery returnedByQuery = new ReturnedByQuery();
            returnedByQuery.setReturnedId((Long) obj[0]);
            returnedByQuery.setReturnedNumber((Long) obj[1]);
            returnedByQuery.setReturnedDescription((String) obj[2]);
            returnedByQuery.setReturnedDate(((Date) obj[3]).toLocalDate());
            returnedByQuery.setReturnedQuantity((Long) obj[4]);
            returnedByQuery.setReturnedUnitPrice((Double) obj[5]);
            returnedByQuery.setReturnedAmount((Double) obj[6]);
            returnedByQuery.setReturnedCustomerName((String) obj[7]);
            return returnedByQuery;
        }).collect(Collectors.toList());
    }


    public ReturnedDto getReturnedById(Long returnedId) {
        List<ReturnedDto> collect = returnedRepository.getReturnedById(returnedId).stream().map(obj -> {
            ReturnedDto returnedDto = new ReturnedDto();
            returnedDto.setId((Long) obj[0]);
            returnedDto.setReturnedNumber((Long) obj[1]);
            returnedDto.setReturnedDescription((String) obj[2]);
            returnedDto.setReturnedDate(((Date) obj[3]).toLocalDate());
            returnedDto.setQuantity((Long) obj[4]);
            returnedDto.setUnitPrice((Double) obj[5]);
            returnedDto.setCustomerId((Long) obj[6]);
            return returnedDto;
        }).toList();
        System.out.println(collect.get(0));
        return collect.get(0);
    }


    public Integer updateReturned(Long returnedId, ReturnedDto returnedDto) {
        if (!returnedRepository.existsById(returnedId))
            throw new EntityNotFoundException("سند برگشت از فروش با شناسه " + returnedId + " یافت نشد.");
        try {
            return returnedRepository.updateReturnedById(
                    returnedDto.getReturnedNumber(),
                    returnedDto.getReturnedDate(),
                    returnedDto.getReturnedDescription(),
                    returnedDto.getQuantity(),
                    returnedDto.getUnitPrice(),
                    new Customer(returnedDto.getCustomerId()),
                    returnedId
            );
        } catch (RuntimeException e) {
            throw new RuntimeException();
        }
    }

    public void deleteReturned(Long returnedId) {
        Optional<Returned> optionalReturned = returnedRepository.findById(returnedId);
        if (optionalReturned.isEmpty()) {
            throw new EntityNotFoundException("سند برگشت  با شناسه " + returnedId + "یافت نشد.");
        }
        Returned returned = optionalReturned.get();

        returnedRepository.delete(returned);
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
    public void importAdjustmentsFromExcel(MultipartFile file) throws IOException {
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);

            for (Row currentRow : sheet) {
                if (currentRow.getRowNum() == 0) {
                    continue;
                }
                Long id = (long) currentRow.getCell(0).getNumericCellValue();
                Long returnedNumber = (long) currentRow.getCell(1).getNumericCellValue();
                String returnedDescription = currentRow.getCell(2).getStringCellValue();
                String returnedDate = currentRow.getCell(3).getStringCellValue();
                Double unitPrice = currentRow.getCell(4).getNumericCellValue();
                Long quantity = (long) currentRow.getCell(5).getNumericCellValue();
                Long customerId = (long) currentRow.getCell(6).getNumericCellValue();


                Returned returned = new Returned();
                returned.setId(id);
                returned.setReturnedNumber(returnedNumber);
                returned.setReturnedDescription(returnedDescription);
                returned.setReturnedDate(convertDate(returnedDate));
                returned.setQuantity(quantity);
                returned.setUnitPrice(unitPrice);
                returned.setCustomer(new Customer(customerId));
                returnedRepository.save(returned);
            }
        }
    }
}

