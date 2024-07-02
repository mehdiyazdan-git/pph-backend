package com.armaninvestment.parsparandreporter.controllers;

import com.armaninvestment.parsparandreporter.dtos.EstablishmentDto;
import com.armaninvestment.parsparandreporter.exceptions.DatabaseIntegrityViolationException;
import com.armaninvestment.parsparandreporter.services.EstablishmentService;
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
@RequestMapping(path = "/api/establishments")
public class EstablishmentController {
    private final EstablishmentService establishmentService;

    @Autowired
    public EstablishmentController(EstablishmentService establishmentService) {
        this.establishmentService = establishmentService;
    }

    @PostMapping(path = {"/", ""})
    public ResponseEntity<?> createEstablishment(@RequestBody EstablishmentDto establishmentDto) {
        try {
            EstablishmentDto createdEstablishment = establishmentService.createEstablishment(establishmentDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdEstablishment);
        } catch (PersistenceException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Error in creation: " + e.getMessage());
        }
    }

    @GetMapping(path = {"/", ""})
    public ResponseEntity<List<EstablishmentDto>> getAllEstablishments() {
        List<EstablishmentDto> establishments = establishmentService.getAllEstablishments();
        return ResponseEntity.ok(establishments);
    }

    @GetMapping(path = "/by-customer-id/{customerId}")
    public ResponseEntity<EstablishmentDto> getEstablishmentByCustomerId(@PathVariable Long customerId) {
        Optional<EstablishmentDto> establishment = establishmentService.getEstablishmentByCustomerId(customerId);
        return establishment.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping(path = "/{establishmentId}")
    public ResponseEntity<EstablishmentDto> getEstablishmentById(@PathVariable Long establishmentId) {
        Optional<EstablishmentDto> establishment = establishmentService.getEstablishmentById(establishmentId);
        return establishment.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping(path = "/{establishmentId}")
    public ResponseEntity<?> updateEstablishment(@PathVariable Long establishmentId, @RequestBody EstablishmentDto establishmentDto) throws IllegalAccessException {
        try {
            establishmentService.updateEstablishment(establishmentId, establishmentDto);
            return ResponseEntity.status(HttpStatus.OK).body(establishmentDto);
        } catch (IllegalAccessException e) {
            System.out.println("exception: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (RuntimeException e) {
            System.out.println("exception: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }

    }

    @DeleteMapping(path = "/{establishmentId}")
    public ResponseEntity<?> deleteEstablishment(@PathVariable Long establishmentId) {
        try {
            establishmentService.deleteEstablishment(establishmentId);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Establishment deleted successfully.");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Establishment with ID " + establishmentId + " not found.");
        } catch (DatabaseIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Server-side error...");
        }
    }
}
