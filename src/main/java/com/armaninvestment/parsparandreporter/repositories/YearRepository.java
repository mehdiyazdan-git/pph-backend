package com.armaninvestment.parsparandreporter.repositories;

import com.armaninvestment.parsparandreporter.entities.Year;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface YearRepository extends JpaRepository<Year, Long> {
    @Query("select y from Year y where y.name = :year")
    Optional<Year> findByYearName(@Param("year") Long year);

    @Query("SELECT COUNT(r.id) FROM Report r  WHERE r.year.id = :year_Id")
    long countReports(@Param("year_Id") Long year_Id);

    @Query("SELECT COUNT(i.id) FROM Invoice i  WHERE i.year.id = :year_Id")
    long countInvoices(@Param("year_Id") Long year_Id);

    @Query("SELECT COUNT(p.id) FROM Payment p  WHERE p.year.id = :year_Id")
    long countPayments(@Param("year_Id") Long year_Id);

    @Query("SELECT COUNT(c.id) FROM Contract c  WHERE c.year.id = :year_Id")
    long countContracts(@Param("year_Id") Long year_Id);

    @Query("SELECT COUNT(a.id) FROM Addendum a  WHERE a.year.id = :year_Id")
    long countAddendum(@Param("year_Id") Long year_Id);

    @Query("SELECT COUNT(wr.id) FROM WarehouseReceipt wr  WHERE wr.year.id = :year_Id")
    long countWareHouseReceipts(@Param("year_Id") Long year_Id);


}