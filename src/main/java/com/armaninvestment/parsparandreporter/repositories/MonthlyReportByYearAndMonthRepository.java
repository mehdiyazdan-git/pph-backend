package com.armaninvestment.parsparandreporter.repositories;

import com.armaninvestment.parsparandreporter.dtos.CompanyReportDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MonthlyReportByYearAndMonthRepository extends JpaRepository<CompanyReportDTO, Long> {
    @Query(value = "SELECT * FROM get_monthly_report_by_year_and_month(:year, :month, cast(:productType as text))", nativeQuery = true)
    List<CompanyReportDTO> getReport(@Param("year") int year, @Param("month") int month, @Param("productType") String productType);
}

