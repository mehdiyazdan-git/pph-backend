package com.armaninvestment.parsparandreporter.repositories;

import com.armaninvestment.parsparandreporter.dtos.ContractAndInvoiceTotalsDTO;
import com.armaninvestment.parsparandreporter.dtos.InvoiceListRowDto;
import com.armaninvestment.parsparandreporter.entities.Invoice;
import com.armaninvestment.parsparandreporter.entities.Year;
import com.armaninvestment.parsparandreporter.enums.SalesType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;


@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long>, JpaSpecificationExecutor<Invoice> {

    @Query(value = "select id,description from search_invoice_by_description_keywords(:searchQuery,:yearId)", nativeQuery = true)
    List<Object[]> searchInvoiceByDescriptionKeywords(@Param("searchQuery") String searchQuery, @Param("yearId") Long yearId);

    @Query(value = "select id,description from invoice_drop_down_by_year_id(:yearId)", nativeQuery = true)
    List<Object[]> invoiceDropDownByYearId(@Param("yearId") Long yearId);

    @Query("SELECT MAX(i.invoiceNumber) FROM Invoice i")
    Long findMaxInvoiceNumber();

    @Query("SELECT i FROM Invoice i WHERE i.contract.id = :contractId ORDER BY i.invoiceNumber ASC")
    List<Invoice> findAllByContractIdSortedByInvoiceNumberAsc(@Param("contractId") Long contractId);

    @Query("SELECT get_invoice_count_for_contract(cast(:contractId as biginteger))")
    long countInvoicesByContractId(Long contractId);

    @Query("select i from Invoice i where i.year = :year order by i.issuedDate asc")
    List<Invoice> findAllByYearOrderByIssuedDateAsc(@Param("year") Year year);

    @Query("select (count(i) > 0) from Invoice i where i.invoiceNumber = :invoiceNumber and i.year = :year")
    boolean existsByInvoiceNumberAndYear(@Param("invoiceNumber") Long invoiceNumber, @Param("year") Year year);

    @Query(value = "select * from check_invoice_exists_by_id(:invoiceId)", nativeQuery = true)
    boolean invoiceExistById(@Param("invoiceId") Long invoiceId);

    @Query("select (count(i) > 0) from Invoice i where i.invoiceNumber = :invoiceNumber and i.issuedDate = :issuedDate")
    boolean existsByInvoiceNumberAndIssuedDate(@Param("invoiceNumber") Long invoiceNumber, @Param("issuedDate") LocalDate issuedDate);

    @Query(value = "SELECT check_invoice_exists(:invoiceNumber, :issuedDate )", nativeQuery = true)
    boolean checkInvoiceExists(Long invoiceNumber, Date issuedDate);

    @Query(value = "select * from get_contract_and_invoice_totals(:contractId,:invoiceNumber)", nativeQuery = true)
    List<Object[]> getContractAndInvoiceTotals(@Param("contractId") Long contractId, @Param("invoiceNumber") Long invoiceNumber);

    default ContractAndInvoiceTotalsDTO getMappedContractAndInvoiceTotals(Long contractId, Long invoiceNumber) {
        List<Object[]> result = getContractAndInvoiceTotals(contractId, invoiceNumber);

        return result.stream()
                .findFirst()
                .map(row -> new ContractAndInvoiceTotalsDTO(
                        (Long) row[0],  // CumulativeContractAmount
                        (Long) row[1],  // CumulativeContractQuantity
                        (Long) row[2],  // ContractAdvancedPayment
                        (Long) row[3],  // ContractPerformanceBond
                        (Long) row[4],  // ContractInsuranceDeposit
                        (Long) row[5],  // TotalCommittedContractAmount
                        (Long) row[6],  // TotalRemainingContractAmount
                        (Long) row[7],  // TotalCommittedContractCount
                        (Long) row[8],  // TotalInvoiceCount
                        (Long) row[9],  // TotalConsumedContractAdvancedPayment
                        (Long) row[10], // TotalOutstandingContractAdvancedPayment
                        (Long) row[11], // TotalCommittedPerformanceBond
                        (Long) row[12], // TotalRemainingPerformanceBond
                        (Long) row[13], // TotalCommittedInsuranceDeposit
                        (Long) row[14]  // TotalRemainingInsuranceDeposit
                ))
                .orElseThrow(() -> new NoSuchElementException("نتیجه‌ای برای contractId=" + contractId + " و invoiceNumber=" + invoiceNumber + " یافت نشد."));
    }

    @Query(value = "select * from get_invoice_list_by_customer_code_and_year_name(cast(:customerCode as text),cast(:yearName as bigint))", nativeQuery = true)
    List<Object[]> getInvoiceListByCustomerCodeAndYearName(String customerCode, Long yearName);

    default List<InvoiceListRowDto> mapToInvoicedListRowDto(String customerCode, Long yearName) {
        List<Object[]> results = getInvoiceListByCustomerCodeAndYearName(customerCode, yearName);

        return results.stream()
                .map(result -> new InvoiceListRowDto(
                        (Long) result[0],      // invoiceId
                        (Long) result[1],      // invoiceNumber
                        (String) result[2],    // issuedDate
                        (String) result[3],    // dueDate
                        (String) result[4],    // customerCode
                        (String) result[5],    // customerName
                        mapSalesType((String) result[6]), // map salesType to PersianCaption
                        (BigDecimal) result[7], // totalAmount
                        (Long) result[8],      // totalQuantity
                        (Long) result[9]       // itemCount
                ))
                .collect(Collectors.toList());
    }

    private String mapSalesType(String salesType) {
        return Arrays.stream(SalesType.values())
                .filter(type -> type.name().equalsIgnoreCase(salesType))
                .findFirst()
                .map(SalesType::getPersianCaption)
                .orElse(salesType); // If no match found, return the original value
    }

    @Query(nativeQuery = true, value = "SELECT * FROM get_invoice_by_id(:invoiceId)")
    List<Object[]> getInvoiceById(@Param("invoiceId") Long invoiceId);

    @Query(nativeQuery = true, value = "SELECT * FROM get_invoice_items_by_invoice_id(:invoiceId)")
    List<Object[]> getInvoiceItemsByInvoiceId(@Param("invoiceId") Long invoiceId);

    @Transactional
    @Modifying
    @Query(value = """
            UPDATE public.invoice
                 SET due_date          = :dueDate,
                     invoice_number    = :invoiceNumber,
                     issued_date       = :issuedDate,
                     sales_type        = :salesType,
                     contract_id       = :contractId,
                     customer_id       = :customerId,
                     invoice_status_id = :invoiceStatusId,
                     year_id           = :yearId,
                     advanced_payment  = :advancedPayment,
                     insurance_deposit = :insuranceDeposit,
                     performance_bound = :performanceBound
                 WHERE id = :invoiceId\s""", nativeQuery = true)
    void updateInvoice(@Param("invoiceNumber") Long invoiceNumber,
                       @Param("issuedDate") LocalDate issuedDate,
                       @Param("dueDate") LocalDate dueDate,
                       @Param("advancedPayment") Long advancedPayment,
                       @Param("performanceBound") Long performanceBound,
                       @Param("insuranceDeposit") Long insuranceDeposit,
                       @Param("contractId") Long contractId,
                       @Param("salesType") String salesType,
                       @Param("customerId") Long customerId,
                       @Param("invoiceStatusId") Integer invoiceStatusId,
                       @Param("yearId") Long yearId,
                       @Param("invoiceId") Long invoiceId);

    @Transactional
    @Modifying
    @Query(value = "delete from public.invoice_item i where i.invoice_id = :invoiceId", nativeQuery = true)
    void deleteInvoiceItems(Long invoiceId);

    @Transactional
    @Modifying
    @Query(value = """
            INSERT INTO public.invoice_item ( quantity, unit_price, invoice_id, product_id, warehouse_receipt_id)
            VALUES ( :quantity, :unitPrice, :invoiceId, :productId, :warehouseReceiptId);""", nativeQuery = true)
    void createInvoiceItem(@Param("productId") Long productId,
                           @Param("quantity") Integer quantity,
                           @Param("unitPrice") Long unitPrice,
                           @Param("invoiceId") Long invoiceId,
                           @Param("warehouseReceiptId") Long warehouseReceiptId);

    @Query(nativeQuery = true, value = "select cast(coalesce(sum(insurance_deposit),0) as double precision) from invoice where customer_id = :customerId")
    Double getInsuranceDepositByCustomerId(Long customerId);

    @Query(nativeQuery = true, value = "select cast(coalesce(sum(performance_bound),0) as double precision) from invoice where customer_id = :customerId")
    Double getPerformanceBoundByCustomerId(Long customerId);

}