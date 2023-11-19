package com.armaninvestment.parsparandreporter.services;

import com.armaninvestment.parsparandreporter.dtos.AddendumDto;
import com.armaninvestment.parsparandreporter.entities.Addendum;
import com.armaninvestment.parsparandreporter.repositories.AddendumRepository;
import com.armaninvestment.parsparandreporter.repositories.ContractRepository;
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
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AddendumService {
    private final AddendumRepository addendumRepository;
    private final ContractRepository contractRepository;

    @Autowired
    public AddendumService(AddendumRepository addendumRepository, ContractRepository contractRepository) {
        this.addendumRepository = addendumRepository;
        this.contractRepository = contractRepository;
    }

    public List<AddendumDto> getAllAddendumList() {
        List<Addendum> addendumList = addendumRepository.findAll();
        return addendumList.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }


    public AddendumDto getAddendumById(Long id) throws EntityNotFoundException {
        Addendum addendum = addendumRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Addendum not found with id: " + id));
        return convertToDto(addendum);
    }


    public AddendumDto createAddendum(AddendumDto addendumDto) {
        Addendum addendum = new Addendum();
        addendum.setAddendumNumber(addendumDto.getAddendumNumber());
        addendum.setContract(contractRepository.findById(addendumDto.getContractId()).orElse(null));
        addendum.setUnitPrice(addendumDto.getUnitPrice());
        addendum.setQuantity(addendumDto.getQuantity());
        addendum.setStartDate(addendumDto.getStartDate());
        addendum.setEndDate(addendumDto.getEndDate());
        addendum = addendumRepository.save(addendum);
        return convertToDto(addendum);
    }


    public AddendumDto updateAddendum(Long id, AddendumDto addendumDto) throws EntityNotFoundException {
        Addendum addendum = addendumRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Addendum not found with id: " + id));
        addendum.setAddendumNumber(addendumDto.getAddendumNumber());
        addendum.setContract(contractRepository.findById(addendumDto.getContractId()).orElse(null));
        addendum.setUnitPrice(addendumDto.getUnitPrice());
        addendum.setQuantity(addendumDto.getQuantity());
        addendum.setStartDate(addendumDto.getStartDate());
        addendum.setEndDate(addendumDto.getEndDate());
        addendum = addendumRepository.save(addendum);
        return convertToDto(addendum);
    }


    public void deleteAddendum(Long id) throws EntityNotFoundException {
        Addendum addendum = addendumRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Addendum not found with id: " + id));

        addendumRepository.delete(addendum);
    }

    private AddendumDto convertToDto(Addendum addendum) {
        AddendumDto addendumDto = new AddendumDto();
        addendumDto.setId(addendum.getId());
        addendumDto.setAddendumNumber(addendum.getAddendumNumber());
        addendumDto.setContractId(addendum.getContract().getId());
        addendumDto.setUnitPrice(addendum.getUnitPrice());
        addendumDto.setQuantity(addendum.getQuantity());
        addendumDto.setStartDate(addendum.getStartDate());
        addendumDto.setEndDate(addendum.getEndDate());
        return addendumDto;
    }

    @Transactional
    public void importAddendumsFromExcel(MultipartFile file) throws IOException {
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0); // Assuming the data is in the first sheet

            for (Row currentRow : sheet) {
                if (currentRow.getRowNum() == 0) {
                    // Skip the header row
                    continue;
                }

                // Extract addendum data from Excel columns, adjust indices as needed
                String addendumNumber = currentRow.getCell(0).getStringCellValue();
                Long unitPrice = (long) currentRow.getCell(1).getNumericCellValue();
                Long quantity = (long) currentRow.getCell(2).getNumericCellValue();
                LocalDate startDate = currentRow.getCell(3).getDateCellValue().toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
                LocalDate endDate = currentRow.getCell(4).getDateCellValue().toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
                // Extract other addendum fields...

                // Create and save a new Addendum entity
                Addendum addendum = new Addendum();
                addendum.setAddendumNumber(addendumNumber);
                addendum.setUnitPrice(unitPrice);
                addendum.setQuantity(quantity);
                addendum.setStartDate(startDate);
                addendum.setEndDate(endDate);
                // Set other addendum fields...
                addendumRepository.save(addendum);
            }
        }
    }

    public XSSFWorkbook addendumListExcel() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet();

        List<AddendumDto> allAddendumList = getAllAddendumList();

        XSSFWorkbook addendumWorkbook = new XSSFWorkbook();
        XSSFSheet addendumSheet = addendumWorkbook.createSheet("Addendums");

        XSSFRow headerRow = addendumSheet.createRow(0);
        headerRow.createCell(0).setCellValue("Addendum Number");
        headerRow.createCell(1).setCellValue("Contract ID");
        headerRow.createCell(2).setCellValue("Unit Price");
        headerRow.createCell(3).setCellValue("Quantity");
        headerRow.createCell(4).setCellValue("Start Date");
        headerRow.createCell(5).setCellValue("End Date");

        int rowNum = 1;
        for (AddendumDto addendum : allAddendumList) {
            XSSFRow row = addendumSheet.createRow(rowNum++);
            row.createCell(0).setCellValue(addendum.getAddendumNumber());
            row.createCell(1).setCellValue(addendum.getContractId());
            row.createCell(2).setCellValue(addendum.getUnitPrice());
            row.createCell(3).setCellValue(addendum.getQuantity());
            row.createCell(4).setCellValue(addendum.getStartDate().toString());
            row.createCell(5).setCellValue(addendum.getEndDate().toString());
        }

        return addendumWorkbook;
    }


}





