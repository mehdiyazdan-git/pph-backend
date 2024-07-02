package com.armaninvestment.parsparandreporter.controllers;

import com.armaninvestment.parsparandreporter.entities.AppSettingDto;
import com.armaninvestment.parsparandreporter.exceptions.DatabaseIntegrityViolationException;
import com.armaninvestment.parsparandreporter.services.AppSettingService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin
@RestController
@RequestMapping(path = "/api/app-settings")
public class AppSettingController {
    private final AppSettingService appSettingService;

    @Autowired
    public AppSettingController(AppSettingService appSettingService) {
        this.appSettingService = appSettingService;
    }

    @PostMapping(path = {"/", ""})
    public ResponseEntity<?> createAppSetting(@RequestBody AppSettingDto appSettingDto) {
        try {
            AppSettingDto createdAppSetting = appSettingService.createAppSetting(appSettingDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdAppSetting);
        } catch (PersistenceException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Error in creation: " + e.getMessage());
        }
    }

    @GetMapping(path = {"/", ""})
    public ResponseEntity<List<AppSettingDto>> getAllAppSettings() {
        List<AppSettingDto> appSettings = appSettingService.getAllAppSettings();
        return ResponseEntity.ok(appSettings);
    }

    @GetMapping(path = "/{appSettingId}")
    public ResponseEntity<AppSettingDto> getAppSettingById(@PathVariable Long appSettingId) {
        Optional<AppSettingDto> appSetting = appSettingService.getAppSettingById(appSettingId);
        return appSetting.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping(path = "/{appSettingId}")
    public ResponseEntity<?> updateAppSetting(@PathVariable Long appSettingId, @RequestBody AppSettingDto appSettingDto) throws IllegalAccessException {
        try {
            appSettingService.updateAppSetting(appSettingId, appSettingDto);
            return ResponseEntity.status(HttpStatus.OK).body(appSettingDto);
        } catch (IllegalAccessException e) {
            System.out.println("exception: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (RuntimeException e) {
            System.out.println("exception: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping(path = "/{appSettingId}")
    public ResponseEntity<?> deleteAppSetting(@PathVariable Long appSettingId) {
        try {
            appSettingService.deleteAppSetting(appSettingId);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("AppSetting deleted successfully.");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("AppSetting with ID " + appSettingId + " not found.");
        } catch (DatabaseIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Server-side error...");
        }
    }
}

