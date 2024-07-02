package com.armaninvestment.parsparandreporter.services;

import com.armaninvestment.parsparandreporter.dtos.AppSettingMapper;
import com.armaninvestment.parsparandreporter.entities.AppSetting;
import com.armaninvestment.parsparandreporter.entities.AppSettingDto;
import com.armaninvestment.parsparandreporter.repositories.AppSettingRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AppSettingService {
    private final AppSettingRepository appSettingRepository;
    private final AppSettingMapper appSettingMapper;


    public AppSettingService(AppSettingRepository appSettingRepository, AppSettingMapper appSettingMapper) {
        this.appSettingRepository = appSettingRepository;
        this.appSettingMapper = appSettingMapper;
    }

    public AppSettingDto createAppSetting(AppSettingDto appSettingDto) {
        AppSetting appSetting = appSettingMapper.toEntity(appSettingDto);
        AppSetting savedAppSetting = appSettingRepository.save(appSetting);
        return appSettingMapper.toDto(savedAppSetting);
    }

    public List<AppSettingDto> getAllAppSettings() {
        return appSettingRepository.findAll().stream().map(appSettingMapper::toDto).collect(Collectors.toList());
    }


    public Optional<AppSettingDto> getAppSettingById(Long appSettingId) {
        Optional<AppSetting> appSetting = appSettingRepository.findById(appSettingId);
        if (appSetting.isPresent()) {
            return appSetting.map(appSettingMapper::toDto);
        }

        return Optional.empty();
    }

    public void updateAppSetting(Long appSettingId, AppSettingDto appSettingDto) throws IllegalAccessException {
        if (!appSettingRepository.existsById(appSettingId)) throw new IllegalAccessException("invalid appSetting id");
        try {
            appSettingRepository.updateAppSettingById(
                    appSettingDto.getVat(),
                    appSettingDto.getId()
            );
            System.out.println("successful update appSetting");
        } catch (Exception e) {
            System.out.println("exception: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void deleteAppSetting(Long appSettingId) {
        Optional<AppSetting> optionalAppSetting = appSettingRepository.findById(appSettingId);
        if (optionalAppSetting.isEmpty()) {
            throw new EntityNotFoundException("AppSetting with ID " + appSettingId + " not found.");
        }
        appSettingRepository.deleteById(appSettingId);
    }
}

