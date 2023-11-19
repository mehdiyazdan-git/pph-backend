package com.armaninvestment.parsparandreporter.repositories;


import com.armaninvestment.parsparandreporter.entities.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

}