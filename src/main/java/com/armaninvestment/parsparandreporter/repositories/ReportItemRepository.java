package com.armaninvestment.parsparandreporter.repositories;


import com.armaninvestment.parsparandreporter.entities.ReportItem;
import com.armaninvestment.parsparandreporter.entities.WarehouseReceipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportItemRepository extends JpaRepository<ReportItem, Long> {
    @Query("select (count(r) > 0) from ReportItem r where r.warehouseReceipt = :warehouseReceipt")
    boolean existsAllByWarehouseReceipt(@Param("warehouseReceipt") WarehouseReceipt warehouseReceipt);

}