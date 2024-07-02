package com.armaninvestment.parsparandreporter.services;

import com.armaninvestment.parsparandreporter.dtos.YearDto;
import com.armaninvestment.parsparandreporter.entities.Year;
import com.armaninvestment.parsparandreporter.exceptions.DatabaseIntegrityViolationException;
import com.armaninvestment.parsparandreporter.mappers.YearMapper;
import com.armaninvestment.parsparandreporter.repositories.YearRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class YearServiceImpl implements YearService {

    private final YearRepository yearRepository;
    private final YearMapper yearMapper;

    @Autowired
    public YearServiceImpl(YearRepository yearRepository, YearMapper yearMapper) {
        this.yearRepository = yearRepository;
        this.yearMapper = yearMapper;
    }

    @Override
    public YearDto createYear(YearDto yearDto) {
        Year year = yearMapper.toEntity(yearDto);
        year = yearRepository.save(year);
        return yearMapper.toDto(year);
    }

    @Override
    public YearDto getYearById(Long id) {
        Year year = yearRepository.findById(id).orElse(null);
        return (year != null) ? yearMapper.toDto(year) : null;
    }

    @Override
    public List<YearDto> getAllYears() {
        List<Year> years = yearRepository.findAllYearsOrderByNameDesc();
        return years.stream()
                .map(yearMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public YearDto updateYear(Long id, YearDto yearDto) {
        Year year = yearRepository.findById(id).orElse(null);
        if (year != null) {
            yearMapper.partialUpdate(yearDto, year);
            year = yearRepository.save(year);
            return yearMapper.toDto(year);
        }
        return null; // Handle not found scenario
    }

    @Override
    public void deleteYear(Long id) {
        Optional<Year> optionalYear = yearRepository.findById(id);
        if (optionalYear.isEmpty()) {
            throw new EntityNotFoundException("سال با شناسه " + id + "یافت نشد.");
        }
        Year year = optionalYear.get();

        if (yearRepository.countPayments(id) > 0) {
            throw new DatabaseIntegrityViolationException("امکان حذف سال وجود ندارد چون پرداخت‌های مرتبط دارد.");
        }
        if (yearRepository.countWareHouseReceipts(id) > 0) {
            throw new DatabaseIntegrityViolationException("امکان حذف سال وجود ندارد چون آیتم‌های حواله های مرتبط دارد.");
        }
        if (yearRepository.countContracts(id) > 0) {
            throw new DatabaseIntegrityViolationException("امکان حذف سال وجود ندارد چون قراردادهای مرتبط دارد.");
        }
        if (yearRepository.countReports(id) > 0) {
            throw new DatabaseIntegrityViolationException("امکان حذف سال وجود ندارد چون گزارش های مرتبط دارد.");
        }
        if (yearRepository.countInvoices(id) > 0) {
            throw new DatabaseIntegrityViolationException("امکان حذف سال وجود ندارد چون فاکتور های مرتبط دارد.");
        }
        if (yearRepository.countAddendum(id) > 0) {
            throw new DatabaseIntegrityViolationException("امکان حذف سال وجود ندارد چون الحاقیه های مرتبط دارد.");
        }
        yearRepository.deleteById(id);
    }
}
