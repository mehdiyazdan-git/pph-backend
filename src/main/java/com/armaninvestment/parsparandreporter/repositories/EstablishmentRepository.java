package com.armaninvestment.parsparandreporter.repositories;

import com.armaninvestment.parsparandreporter.entities.Customer;
import com.armaninvestment.parsparandreporter.entities.Establishment;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EstablishmentRepository extends JpaRepository<Establishment, Long> {
    @Query("select e from Establishment e where e.customer = :customer")
    Optional<Establishment> findByCustomer(@Param("customer") Customer customer);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "UPDATE public.establishment SET\t" +
            "claims = :claims, " +
            "customer_id = :customerId " +
            "WHERE id = :id ")
    void updateEstablishmentById(@Param("claims") Double claims,
                                 @Param("customerId") Long customerId,
                                 @Param("id") Long id);
}