package com.armaninvestment.parsparandreporter.repositories;

import com.armaninvestment.parsparandreporter.entities.Customer;
import com.armaninvestment.parsparandreporter.entities.Returned;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReturnedRepository extends JpaRepository<Returned, Long> {
    @Query(value = "select * from get_all_returned()", nativeQuery = true)
    List<Object[]> getAllReturned();

    @Query(value = "select * from get_returned_by_id(:returnedId)", nativeQuery = true)
    List<Object[]> getReturnedById(Long returnedId);

    @Transactional
    @Modifying
    @Query("""
            update Returned r set r.returnedNumber = :returnedNumber, r.returnedDate = :returnedDate,
              r.returnedDescription = :returnedDescription, r.quantity = :quantity, r.unitPrice = :unitPrice,
              r.customer = :customer
            where r.id = :id""")
    int updateReturnedById(
            @Param("returnedNumber") Long returnedNumber,
            @Param("returnedDate") LocalDate returnedDate,
            @Param("returnedDescription") String returnedDescription,
            @Param("quantity") Long quantity,
            @Param("unitPrice") Double unitPrice,
            @Param("customer") Customer customer,
            @Param("id") Long id
    );

    @Transactional
    @Modifying
    @Query(value = "INSERT INTO returned (returned_number, returned_date, returned_description, quantity, unit_price, customer_id) " +
            "VALUES (:returnedNumber, :returnedDate, :returnedDescription, :quantity, :unitPrice, :customerId)",
            nativeQuery = true)
    void createReturned(
            @Param("returnedNumber") Long returnedNumber,
            @Param("returnedDate") LocalDate returnedDate,
            @Param("returnedDescription") String returnedDescription,
            @Param("quantity") Long quantity,
            @Param("unitPrice") Double unitPrice,
            @Param("customerId") Long customerId
    );

}