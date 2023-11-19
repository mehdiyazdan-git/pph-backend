package com.armaninvestment.parsparandreporter.controllers;


import com.armaninvestment.parsparandreporter.dtos.AddendumDto;
import com.armaninvestment.parsparandreporter.services.AddendumService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping(path = "/api/addendums")
public class AddendumController {

    private final AddendumService addendumService;

    @Autowired
    public AddendumController(AddendumService addendumService) {
        this.addendumService = addendumService;
    }

    @GetMapping(path = {"/", ""})
    public ResponseEntity<List<AddendumDto>> findAll() {
        List<AddendumDto> allAddendumList = addendumService.getAllAddendumList();
        return ResponseEntity.ok(allAddendumList);
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<AddendumDto> findById(@PathVariable Long id) {
        AddendumDto addendumDto = addendumService.getAddendumById(id);
        if (addendumDto != null) {
            return ResponseEntity.ok(addendumDto);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping(path = {"/", ""})
    public ResponseEntity<?> create(@RequestBody AddendumDto addendumDto) {
        try {
            AddendumDto createdAddendum = addendumService.createAddendum(addendumDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdAddendum);
        } catch (PersistenceException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("خطا در ایجاد: " + e.getMessage());
        }
    }


    @PutMapping("/{id}")
    public ResponseEntity<AddendumDto> update(@PathVariable Long id, @RequestBody AddendumDto addendumDto) {
        if (addendumDto.getId() == null || !addendumDto.getId().equals(id)) {
            return ResponseEntity.badRequest().build();
        } else {
            addendumDto = addendumService.updateAddendum(id, addendumDto);
            if (addendumDto != null) {
                return ResponseEntity.ok(addendumDto);
            } else {
                return ResponseEntity.notFound().build();
            }
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        try {
            addendumService.deleteAddendum(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/import")
    public ResponseEntity<String> importAddendums(@RequestParam("file") MultipartFile file) {
        try {
            addendumService.importAddendumsFromExcel(file);
            return ResponseEntity.ok("Addendums imported successfully"); // Return a success message
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("File upload failed: " + e.getMessage()); // Return an error message
        }
    }

}

