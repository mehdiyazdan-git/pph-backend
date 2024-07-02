package com.armaninvestment.parsparandreporter.repositories;

import com.armaninvestment.parsparandreporter.entities.WarehouseInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface WarehouseInvoiceRepository extends JpaRepository<WarehouseInvoice, Long> {
    @Transactional
    @Modifying
    @Query("update WarehouseInvoice w set w.invoiceId = null where w.invoiceId = :invoiceId")
    void updateInvoiceIdToNullByInvoiceId(@Param("invoiceId") Long invoiceId);

    @Transactional
    @Modifying
    @Query("update WarehouseInvoice w set w.invoiceId = :invoiceId where w.receiptId = :receiptId")
    int updateInvoiceIdByReceiptId(@Param("invoiceId") Long invoiceId, @Param("receiptId") Long receiptId);

    @Query(nativeQuery = true, value = """
            select
             cast(coalesce(sum(wri.quantity * wri.unit_price),0) as double precision),
             cast(coalesce(sum(wri.quantity),0) as double precision)
              from  warehouse_invoice\s
            join warehouse_receipt wr on warehouse_invoice.receipt_id = wr.id \s
            join warehouse_receipt_item wri on wr.id = wri.warehouse_receipt_id \s
            where warehouse_invoice.invoice_id is null\s
            and customer_id = :customerId\s""")
    List<Object[]> getNotInvoicedAmountByCustomerId(Long customerId);


    @Query("select w from WarehouseInvoice w where w.receiptId = :receiptId")
    Optional<WarehouseInvoice> findWarehouseInvoiceByReceiptId(@Param("receiptId") Long receiptId);


    @Query(value = "SELECT " +
            "wr.warehouse_receipt_number AS warehouseReceiptNumber, " +
            "wr.warehouse_receipt_description AS warehouseReceiptDescription, " +
            "gregorian_to_persian(wr.warehouse_receipt_date) AS receiptDate, " +
            "CAST(COALESCE(SUM(wri.quantity * wri.unit_price), 0) AS double precision) AS totalValue, " +
            "CAST(COALESCE(SUM(wri.quantity), 0) AS double precision) AS totalQuantity " +
            "FROM warehouse_receipt wr " +
            "JOIN warehouse_receipt_item wri ON wr.id = wri.warehouse_receipt_id " +
            "JOIN warehouse_invoice wi ON wr.id = wi.receipt_id " +
            "WHERE wi.invoice_id IS NULL " +
            "AND wr.customer_id = :customerId " +
            "GROUP BY wr.warehouse_receipt_number, wr.warehouse_receipt_date, wr.warehouse_receipt_description",
            nativeQuery = true)
    List<Object[]> findNotInvoicedReceiptsByCustomerId(Long customerId);
}