package com.armaninvestment.parsparandreporter.services;

import com.armaninvestment.parsparandreporter.dtos.PaymentDto;
import com.armaninvestment.parsparandreporter.dtos.list.PaymentDTO;
import com.armaninvestment.parsparandreporter.entities.Payment;
import com.armaninvestment.parsparandreporter.entities.Year;
import com.armaninvestment.parsparandreporter.mappers.PaymentMapper;
import com.armaninvestment.parsparandreporter.repositories.CustomerRepository;
import com.armaninvestment.parsparandreporter.repositories.PaymentRepository;
import com.armaninvestment.parsparandreporter.repositories.YearRepository;
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
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final CustomerRepository customerRepository;
    private final YearRepository yearRepository;
    private final PaymentMapper paymentMapper;

    @Autowired
    public PaymentService(PaymentRepository paymentRepository, CustomerRepository customerRepository, YearRepository yearRepository, PaymentMapper paymentMapper) {
        this.paymentRepository = paymentRepository;
        this.customerRepository = customerRepository;
        this.yearRepository = yearRepository;
        this.paymentMapper = paymentMapper;
    }

    public PaymentDto findById(long paymentId) {
        Payment payment = paymentRepository.findById(paymentId).orElseThrow(() -> new EntityNotFoundException("پرداخت با شناسه " + paymentId + " یافت نشد."));
        return paymentMapper.toDto(payment);
    }

    public List<PaymentDTO> getPaymentsByCustomerId(Long customerId) {
        List<Object[]> paymentObjects = paymentRepository.getPaymentsByCustomerId(customerId);

        return paymentObjects.stream().map(obj -> {
            PaymentDTO paymentDTO = new PaymentDTO();
            paymentDTO.setId((Long) obj[0]);
            paymentDTO.setPaymentDate((String) obj[1]);
            paymentDTO.setPaymentDescription((String) obj[2]);
            paymentDTO.setPaymentSubject((String) obj[3]);
            paymentDTO.setPaymentAmount((Long) obj[4]);
            paymentDTO.setCustomerId((Long) obj[5]);
            return paymentDTO;
        }).collect(Collectors.toList());
    }

    @Transactional
    public PaymentDto createPayment(PaymentDto paymentDto) {

        if (!customerRepository.existsById(paymentDto.getCustomerId()))
            throw new EntityNotFoundException("مشتری با شناسه " + paymentDto.getCustomerId() + " یافت نشد.");
        Long paymentAmount = paymentDto.getAmount();
        LocalDate paymentDate = paymentDto.getDate();
        String paymentDescription = paymentDto.getDescription();
        String paymentSubject = paymentDto.getSubject();
        Long customerId = paymentDto.getCustomerId();
        Long yearId = this.getYear(paymentDto.getYearId()).getId();

        paymentRepository.insertPayment(paymentAmount, paymentDate, paymentDescription, paymentSubject, customerId, yearId);

        return paymentDto;
    }

    public void updatePayment(PaymentDto paymentDto) {
        if (!paymentRepository.existsById(paymentDto.getId()))
            throw new EntityNotFoundException("پرداخت با شناسه " + paymentDto.getId() + " یافت نشد.");
        if (!customerRepository.existsById(paymentDto.getCustomerId()))
            throw new EntityNotFoundException("مشتری با شناسه " + paymentDto.getCustomerId() + " یافت نشد.");
        try {
            paymentRepository.updatePaymentById(
                    paymentDto.getAmount(),
                    paymentDto.getDate(),
                    paymentDto.getDescription(),
                    paymentDto.getCustomerId(),
                    paymentDto.getYearId(),
                    paymentDto.getSubject(),
                    paymentDto.getId()
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deletePayment(Long paymentId) {
        try {
            if (!paymentRepository.existsById(paymentId)) {
                throw new EntityNotFoundException("پرداخت با شناسه " + paymentId + " یافت نشد.");
            }
            paymentRepository.deleteByPaymentId(paymentId);
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
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
                Long amount = (long) currentRow.getCell(2).getNumericCellValue();
                String subject = currentRow.getCell(3).getStringCellValue();
                // Extract other payment fields...

                // Create and save a new Payment entity
                Payment payment = new Payment();
                payment.setDescription(description);
                payment.setDate(date);
                payment.setAmount(amount);
                payment.setSubject(subject);
                paymentRepository.save(payment);
            }
        }
    }

    protected Year getYear(Long yearName) {
        Optional<Year> optionalYear = yearRepository.findByYearName(yearName);
        return optionalYear.orElseThrow(() -> new EntityNotFoundException("سال با مقدار " + yearName + " یافت نشد."));
    }
}

