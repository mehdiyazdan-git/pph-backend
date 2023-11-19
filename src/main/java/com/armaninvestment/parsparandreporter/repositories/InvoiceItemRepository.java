package com.armaninvestment.parsparandreporter.repositories;

import com.armaninvestment.parsparandreporter.entities.InvoiceItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InvoiceItemRepository extends JpaRepository<InvoiceItem, Long> {
    @Query("select i from InvoiceItem i where i.warehouseReceipt.id = :warehouseReceipt_id")
    Optional<InvoiceItem> findInvoiceItemByWarehouseReceiptId(@Param("warehouseReceipt_id") Long warehouseReceipt_id);
}