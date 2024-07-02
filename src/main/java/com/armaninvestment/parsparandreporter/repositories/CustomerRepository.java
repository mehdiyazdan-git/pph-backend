package com.armaninvestment.parsparandreporter.repositories;


import com.armaninvestment.parsparandreporter.dtos.dropdowns.CustomerDropDownDto;
import com.armaninvestment.parsparandreporter.entities.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    @Query(value = "select name from customer where id = :customerId ", nativeQuery = true)
    String getCustomerNameById(Long customerId);

    @Query(value = "select * from check_customer_exists_by_id(:customerId)", nativeQuery = true)
    boolean checkCustomerExistsById(Long customerId);

    @Query(value = "SELECT * FROM get_customer_by_id(?1)", nativeQuery = true)
    List<Object[]> findCustomerById(Long customerId);

    @Query(value = "SELECT * FROM get_payments_by_customer_id(?1)", nativeQuery = true)
    List<Object[]> findPaymentsByCustomerId(Long customerId);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM public.payment WHERE customer_id = ?1)", nativeQuery = true)
    boolean existsCustomerByIdAndPaymentsIsNotEmpty(Long customerId);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM public.report_item WHERE customer_id = ?1)", nativeQuery = true)
    boolean existsCustomerByIdAndReportItemsIsNotEmpty(Long customerId);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM public.contracts WHERE customer_id = ?1)", nativeQuery = true)
    boolean existsCustomerByIdAndContractsIsNotEmpty(Long customerId);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM public.warehouse_receipt WHERE customer_id = ?1)", nativeQuery = true)
    boolean existsCustomerByIdAndWarehouseReceiptsIsNotEmpty(Long customerId);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM public.invoice WHERE customer_id = ?1)", nativeQuery = true)
    boolean existsCustomerByIdAndInvoicesIsNotEmpty(Long customerId);


    @Query("select c from Customer c where c.nationalCode = :nationalCode")
    Customer findByNationalCode(@Param("nationalCode") String nationalCode);

    @Query(value = "SELECT * FROM get_customer_invoice_monthly_summary(:customerId,:yearName)", nativeQuery = true)
    List<Object[]> getCustomerInvoiceMonthlySummary(
            @Param("customerId") Long customerId,
            @Param("yearName") Long yearName);

    @Query("select c from Customer c where c.name like concat('%', :name, '%')")
    List<Customer> findByNameContaining(@Param("name") String name);

    @Query(value = "SELECT get_customer_id_by_customer_code(:customerCode)", nativeQuery = true)
    Long getCustomerIdByCustomerCode(String customerCode);

    @Query(nativeQuery = true, value = "SELECT * FROM get_sales_by_customer_and_year_group_by_month(cast(:customerId as bigint), cast(:year as smallint))")
    List<Object[]> findMonthlySalesByCustomerAndPersianYear(BigInteger customerId, Short year);

    @Query(nativeQuery = true, value = "SELECT * FROM calculate_customer_totals(:customerId,:year)")
    List<Object[]> calculateCustomerTotals(Long customerId, Long year);

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

    @Query(nativeQuery = true, value = "SELECT * FROM get_customer_dropdown()")
    List<Object[]> getCustomerDropDown();

    default List<CustomerDropDownDto> mapToDtoList() {
        return getCustomerDropDown().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private CustomerDropDownDto mapToDto(Object[] row) {
        return new CustomerDropDownDto(
                (Long) row[0],
                (String) row[1]
        );
    }

    @Query(value = "select " +
            "       coalesce(i.id,0) as invoiceId, " +
            "       coalesce(invoice_number,0) as invoiceNumber, " +
            "       coalesce(gregorian_to_persian(issued_date + INTERVAL '1 DAY'),'بدون مقدار') as issuedDate, " +
            "       coalesce(gregorian_to_persian(due_date + INTERVAL '1 DAY'),'بدون مقدار') as dueDate, " +
            "       coalesce(i.sales_type,'CASH_SALES') as salesType, " +
            "       coalesce(contract_number,'نقدی') as contractNumber, " +
            "       coalesce(invoice_status_id,1) as statusId, " +
            "       coalesce(y.name,0) as year, " +
            "       coalesce(c.customer_code,'بدون مقدار') as customerCode, " +
            "       coalesce(i.advanced_payment,0) as advancedPayment, " +
            "       coalesce(i.performance_bound,0) as performanceBound, " +
            "       coalesce(i.insurance_deposit,0) as insuranceDeposit, " +
            "       coalesce(product_code,'بدون مقدار') as productCode, " +
            "       coalesce(ii.quantity,0) as quantity, " +
            "       coalesce(ii.unit_price,0) as unitPrice, " +
            "       coalesce(warehouse_receipt_number,0) as warehouseReceiptNumber, " +
            "       coalesce(gregorian_to_persian(warehouse_receipt_date + INTERVAL '1 DAY'),'بدون مقدار') as warehouseReceiptDate " +
            "from customer c " +
            "         left join invoice i on c.id = i.customer_id " +
            "         left join year y on y.id = i.year_id " +
            "         left join invoice_item ii on i.id = ii.invoice_id " +
            "         left join product p on p.id = ii.product_id " +
            "         left join contracts c2 on c2.id = i.contract_id " +
            "         left join warehouse_receipt wr on wr.id = ii.warehouse_receipt_id " +
            "where c.id = :customerId ", nativeQuery = true)
    List<Object[]> getInvoiceUploadDto(Long customerId);


}