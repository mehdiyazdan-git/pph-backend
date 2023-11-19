package com.armaninvestment.parsparandreporter.repositories;


import com.armaninvestment.parsparandreporter.entities.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvoiceStatusRepository extends JpaRepository<InvoiceStatus, Integer> {
    @Query("select i from InvoiceStatus i where i.name like concat('%', ?1, '%')")
    List<InvoiceStatus> findByNameContains(String name);

}