package com.armaninvestment.parsparandreporter.repositories;


import com.armaninvestment.parsparandreporter.entities.ReportItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReportItemRepository extends JpaRepository<ReportItem, Long> {
    @Query("select r from ReportItem r where r.warehouseReceipt.id = :warehouseReceipt_id")
    Optional<ReportItem> findReportItemByWarehouseReceiptId(@Param("warehouseReceipt_id") Long warehouseReceipt_id);

}