package com.armaninvestment.parsparandreporter.repositories;


import com.armaninvestment.parsparandreporter.entities.Report;
import com.armaninvestment.parsparandreporter.entities.Year;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    Page<Report> findByDateBetweenOrderByDateDesc(LocalDate fromDate, LocalDate toDate, Pageable paging);

    Page<Report> findByDateAfterOrderByDateDesc(LocalDate fromDate, Pageable paging);

    Page<Report> findByDateBeforeOrderByDateDesc(LocalDate toDate, Pageable paging);

    @Query("select r from Report r where r.year = :year order by r.date asc")
    List<Report> findAllByYearOrderByDateAsc(@Param("year") Year year);

    @Query(value = "select * from get_sales_by_year_group_by_month_filter_by_product_type(CAST(:yearId AS smallint),CAST(:productType AS text))", nativeQuery = true)
    List<Object[]> getSalesByYearGroupByMonth(@Param("yearId") Short yearId, @Param("productType") String productType);

    @Query(value = "select * from get_all_reports_by_year_id(:yearId)", nativeQuery = true)
    List<Object[]> getAllReportsByYearId(@Param("yearId") Long yearId);

    @Query(value = "select * from get_report_items_by_report_id(:reportId)", nativeQuery = true)
    List<Object[]> getReportItemsByReportId(@Param("reportId") Long reportId);

    @Query(nativeQuery = true, value = "SELECT * FROM get_report_by_id(:reportId)")
    List<Object[]> getReportById(@Param("reportId") Long reportId);

    @Transactional
    @Modifying
    @Query(value = """
            UPDATE public.report
                 SET report_date          = :reportDate,
                     report_explanation    = :reportExplanation
                 WHERE id = :reportId\s""", nativeQuery = true)
    void updateReport(@Param("reportExplanation") String reportExplanation,
                      @Param("reportDate") LocalDate issuedDate,
                      @Param("reportId") Long performanceBound);

    @Transactional
    @Modifying
    @Query(value = "delete from public.report_item ri where ri.report_id = :reportId", nativeQuery = true)
    void deleteReportItems(Long reportId);

    @Transactional
    @Modifying
    @Query(value = """
            INSERT INTO public.report_item ( quantity, unit_price, report_id, customer_id, warehouse_receipt_id)
            VALUES ( :quantity, :unitPrice, :reportId, :customerId, :warehouseReceiptId);""", nativeQuery = true)
    void createReportItem(@Param("customerId") Long productId,
                          @Param("quantity") Integer quantity,
                          @Param("unitPrice") Long unitPrice,
                          @Param("reportId") Long invoiceId,
                          @Param("warehouseReceiptId") Long warehouseReceiptId);


}