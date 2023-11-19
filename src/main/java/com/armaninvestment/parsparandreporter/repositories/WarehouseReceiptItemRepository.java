package com.armaninvestment.parsparandreporter.repositories;

import com.armaninvestment.parsparandreporter.entities.WarehouseReceiptItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WarehouseReceiptItemRepository extends JpaRepository<WarehouseReceiptItem, Long> {

}