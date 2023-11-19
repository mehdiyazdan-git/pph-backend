package com.armaninvestment.parsparandreporter.repositories;


import com.armaninvestment.parsparandreporter.entities.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    @Query("select c from Customer c where c.name like concat('%', :name, '%')")
    List<Customer> findByNameContaining(@Param("name") String name);

    @Query(nativeQuery = true, value = "SELECT * FROM get_sales_by_customer_and_year_group_by_month(cast(:customerId as bigint), cast(:year as smallint))")
    List<Object[]> findMonthlySalesByCustomerAndPersianYear(BigInteger customerId, Short year);

    @Query(nativeQuery = true, value = "SELECT * FROM get_sales_by_year_group_by_month(cast(:year as smallint))")
    List<Object[]> findMonthlySalesByPersianYear(Short year);

    @Query(nativeQuery = true, value = "SELECT * FROM get_payments_by_customer_and_year_group_by_month(cast(:customerId as bigint), cast(:year as smallint))")
    List<Object[]> findMonthlyPaymentsByCustomerAndPersianYear(BigInteger customerId, Short year);

    @Query(nativeQuery = true, value = "SELECT * FROM get_payments_by_year_group_by_month(cast(:year as smallint))")
    List<Object[]> findMonthlyPaymentsByPersianYear(Short year);

    @Query(nativeQuery = true, value = "SELECT monthly_sales_by_month_and_year(cast(:customerId as integer), cast(:year as integer))")
    List<Object[]> getMonthlyReport(Integer customerId, Integer year);

    @Query("select c from Customer c where c.customerCode = ?1")
    Optional<Customer> findByCustomerCode(String customerCode);

    @Query(value = "select * from get_customer_reports_by_year_and_customer_code(cast(:customerCode as text),:yearName)", nativeQuery = true)
    List<Object[]> getCustomerReportsByYearAndCustomerId(@Param("customerCode") String customerCode, @Param("yearName") Long yearName);

    @Query(value = "select * from get_customer_invoices_by_year_and_customer_code(cast(:customerCode as text),:yearName)", nativeQuery = true)
    List<Object[]> getCustomerInvoicesByYearAndCustomerCode(@Param("customerCode") String customerCode, @Param("yearName") Long yearName);

    @Query(value = "select * from get_payments_by_customer_code_and_year_name(cast(:customerCode as text), cast(:yearName as bigint))", nativeQuery = true)
    List<Object[]> getPaymentsByCustomerCodeAndYearName(@Param("customerCode") String customerCode, @Param("yearName") Long yearName);


}