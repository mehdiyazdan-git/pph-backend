package com.armaninvestment.parsparandreporter.repositories;

import com.armaninvestment.parsparandreporter.entities.Adjustment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AdjustmentRepository extends JpaRepository<Adjustment, Long> {

    @Query(nativeQuery = true, value = "SELECT COALESCE(SUM(CASE\t" +
            "                        WHEN adjustment_type = 'POSITIVE' THEN unit_price * quantity\t" +
            "                        WHEN adjustment_type = 'NEGATIVE' THEN (unit_price * quantity) * -1\t" +
            "                        ELSE 0\t" +
            "    END), 0)\t" +
            "FROM adjustment a\t" +
            "LEFT JOIN invoice i ON i.id = a.invoice_id\t" +
            "LEFT JOIN customer c ON c.id = i.customer_id\t" +
            "LEFT JOIN year y ON y.id = i.year_id\t" +
            "WHERE c.id IS NULL  OR c.id = :customerId\t" +
            "AND y.name is null or y.name = :yearName\t" +
            "AND get_persian_month(gregorian_to_persian(a.adjustment_date)) = :month")
    Long getTotalAdjustmentAmountByYearNameAndMonthAndCustomerId(
            @Param("customerId") Long customerId,
            @Param("yearName") Long yearName,
            @Param("month") Integer month
    );

    @Query(value = "select * from get_adjustments()", nativeQuery = true)
    List<Object[]> getAdjustments();

    @Query(value = "select * from get_adjustment_by_id(:adjustmentId)", nativeQuery = true)
    List<Object[]> getAdjustmentById(Long adjustmentId);

    @Transactional
    @Modifying
    @Query(value = "INSERT INTO adjustment (adjustment_number, adjustment_date, description, unit_price, quantity, adjustment_type, invoice_id) " +
            "VALUES (:adjustmentNumber, :adjustmentDate, :description, :unitPrice, :quantity, :adjustmentType, :invoiceId)",
            nativeQuery = true)
    void createAdjustment(
            @Param("adjustmentNumber") Long adjustmentNumber,
            @Param("adjustmentDate") LocalDate adjustmentDate,
            @Param("description") String description,
            @Param("unitPrice") Double unitPrice,
            @Param("quantity") Long quantity,
            @Param("adjustmentType") String adjustmentType,
            @Param("invoiceId") Long invoiceId
    );

    @Transactional
    @Modifying
    @Query(value = "UPDATE adjustment SET adjustment_number = :adjustmentNumber, adjustment_date = :adjustmentDate, " +
            "description = :description, unit_price = :unitPrice, quantity = :quantity, adjustment_type = :adjustmentType, " +
            "invoice_id = :invoiceId WHERE id = :id",
            nativeQuery = true)
    void updateAdjustment(
            @Param("adjustmentNumber") Long adjustmentNumber,
            @Param("adjustmentDate") LocalDate adjustmentDate,
            @Param("description") String description,
            @Param("unitPrice") Double unitPrice,
            @Param("quantity") Long quantity,
            @Param("adjustmentType") String adjustmentType,
            @Param("invoiceId") Long invoiceId,
            @Param("id") Long id
    );

}