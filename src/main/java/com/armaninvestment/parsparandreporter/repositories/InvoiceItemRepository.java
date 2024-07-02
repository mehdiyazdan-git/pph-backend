package com.armaninvestment.parsparandreporter.repositories;

import com.armaninvestment.parsparandreporter.entities.InvoiceItem;
import com.armaninvestment.parsparandreporter.entities.WarehouseReceipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface InvoiceItemRepository extends JpaRepository<InvoiceItem, Long> {
    @Query("select (count(i) > 0) from InvoiceItem i where i.warehouseReceipt = :warehouseReceipt")
    boolean existsAllByWarehouseReceipt(@Param("warehouseReceipt") WarehouseReceipt warehouseReceipt);


}