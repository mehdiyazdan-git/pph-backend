package com.armaninvestment.parsparandreporter.services;

import com.armaninvestment.parsparandreporter.dtos.EstablishmentDto;
import com.armaninvestment.parsparandreporter.entities.Customer;
import com.armaninvestment.parsparandreporter.entities.Establishment;
import com.armaninvestment.parsparandreporter.mappers.EstablishmentMapper;
import com.armaninvestment.parsparandreporter.repositories.CustomerRepository;
import com.armaninvestment.parsparandreporter.repositories.EstablishmentRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EstablishmentService {
    private final EstablishmentRepository establishmentRepository;
    private final EstablishmentMapper establishmentMapper;
    private final CustomerRepository customerRepository;

    @Autowired
    public EstablishmentService(EstablishmentRepository establishmentRepository, EstablishmentMapper establishmentMapper,
                                CustomerRepository customerRepository) {
        this.establishmentRepository = establishmentRepository;
        this.establishmentMapper = establishmentMapper;
        this.customerRepository = customerRepository;
    }

    public EstablishmentDto createEstablishment(EstablishmentDto establishmentDto) {
        Establishment establishment = establishmentMapper.toEntity(establishmentDto);
        Establishment savedEstablishment = establishmentRepository.save(establishment);
        return establishmentMapper.toDto(savedEstablishment);
    }

    public List<EstablishmentDto> getAllEstablishments() {
        return establishmentRepository.findAll().stream().map(establishmentMapper::toDto).collect(Collectors.toList());
    }

    public Optional<EstablishmentDto> getEstablishmentByCustomerId(Long customerId) {
        Optional<Establishment> establishment = establishmentRepository.findByCustomer(new Customer(customerId));
        if (establishment.isPresent()) {
            return establishment.map(establishmentMapper::toDto);
        }

        return Optional.empty();
    }

    public Optional<EstablishmentDto> getEstablishmentById(Long establishmentId) {
        Optional<Establishment> establishment = establishmentRepository.findById(establishmentId);
        if (establishment.isPresent()) {
            return establishment.map(establishmentMapper::toDto);
        }

        return Optional.empty();
    }

    public void updateEstablishment(Long establishmentId, EstablishmentDto establishmentDto) throws IllegalAccessException {
        if (!establishmentRepository.existsById(establishmentId))
            throw new IllegalAccessException("invalid establishment id");
        if (!customerRepository.existsById(establishmentDto.getCustomerId()))
            throw new IllegalAccessException("invalid customer id");
        try {
            establishmentRepository.updateEstablishmentById(
                    establishmentDto.getClaims(),
                    establishmentDto.getCustomerId(),
                    establishmentDto.getId()
            );
            System.out.println("successful update establishment");
        } catch (Exception e) {
            System.out.println("exception: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void deleteEstablishment(Long establishmentId) {
        Optional<Establishment> optionalEstablishment = establishmentRepository.findById(establishmentId);
        if (optionalEstablishment.isEmpty()) {
            throw new EntityNotFoundException("Establishment with ID " + establishmentId + " not found.");
        }
        establishmentRepository.deleteById(establishmentId);
    }
}
