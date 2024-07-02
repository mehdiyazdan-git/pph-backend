package com.armaninvestment.parsparandreporter.services;


import com.armaninvestment.parsparandreporter.dtos.InvoiceStatusDto;
import com.armaninvestment.parsparandreporter.entities.InvoiceStatus;
import com.armaninvestment.parsparandreporter.exceptions.DatabaseIntegrityViolationException;
import com.armaninvestment.parsparandreporter.mappers.InvoiceStatusMapper;
import com.armaninvestment.parsparandreporter.repositories.InvoiceStatusRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class InvoiceStatusService {

    private final InvoiceStatusRepository invoiceStatusRepository;
    private final InvoiceStatusMapper invoiceStatusMapper;

    @Autowired
    public InvoiceStatusService(InvoiceStatusRepository invoiceStatusRepository, InvoiceStatusMapper invoiceStatusMapper) {
        this.invoiceStatusRepository = invoiceStatusRepository;
        this.invoiceStatusMapper = invoiceStatusMapper;
    }

    public List<InvoiceStatusDto> searchStatusesForDropdown(String searchQuery) {
        List<InvoiceStatus> matchingStatuses = invoiceStatusRepository.findByNameContains(searchQuery);
        return matchingStatuses.stream()
                .map(invoiceStatusMapper::toDto)
                .toList();
    }


    public InvoiceStatusDto createInvoiceStatus(InvoiceStatusDto invoiceStatusDto) {
        InvoiceStatus invoiceStatus = invoiceStatusMapper.toEntity(invoiceStatusDto);
        return invoiceStatusMapper.toDto(invoiceStatusRepository.save(invoiceStatus));
    }


    public List<InvoiceStatusDto> getAllInvoiceStatuses() {
        return invoiceStatusRepository.mapToDtoList();
    }

    public Optional<InvoiceStatusDto> getInvoiceStatusById(Integer id) {
        Optional<InvoiceStatus> invoiceStatus = invoiceStatusRepository.findById(id);
        return invoiceStatus.map(invoiceStatusMapper::toDto);
    }


    public InvoiceStatusDto updateInvoiceStatus(InvoiceStatusDto invoiceStatusDto) {
        Integer id = invoiceStatusDto.getId();
        Optional<InvoiceStatus> optionalInvoiceStatus = invoiceStatusRepository.findById(id);

        if (id == null || optionalInvoiceStatus.isEmpty()) {
            throw new EntityNotFoundException("InvoiceStatus with ID " + id + " not found");
        }

        InvoiceStatus existingInvoiceStatus = optionalInvoiceStatus.get();
        invoiceStatusMapper.partialUpdate(invoiceStatusDto, existingInvoiceStatus);

        return invoiceStatusMapper.toDto(invoiceStatusRepository.save(existingInvoiceStatus));
    }


    public void deleteInvoiceStatus(Integer id) throws InterruptedException {
        try {
            Optional<InvoiceStatus> optionalInvoiceStatus = invoiceStatusRepository.findById(id);

            if (optionalInvoiceStatus.isPresent()) {
                if (!optionalInvoiceStatus.get().getInvoices().isEmpty()) {
                    throw new DatabaseIntegrityViolationException("امکان حذف وضعیت وجود ندارد چون فاکتور های مرتبط دارد.");
                }
                invoiceStatusRepository.deleteById(id);
            } else {
                throw new EntityNotFoundException("حواله با شناسه " + id + " یافت نشد.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new InterruptedException("خطا در حذف وضعیت با شناسه  " + id);
        }
    }
}
