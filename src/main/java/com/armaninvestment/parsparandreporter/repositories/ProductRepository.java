package com.armaninvestment.parsparandreporter.repositories;

import com.armaninvestment.parsparandreporter.entities.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query("select p from Product p")
    Page<Product> getPage(Pageable pageable);

    @Query(value = "select * from check_product_exists_by_id(:productId)", nativeQuery = true)
    boolean checkProductExistsById(Long productId);

    @Query(value = "SELECT get_product_id_by_product_code(:productCode)", nativeQuery = true)
    Long getProductIdByProductCode(String productCode);

    @Query("select p from Product p where p.productName like concat('%', :productName, '%')")
    List<Product> findByProductNameContains(@Param("productName") String productName);

    @Query("select p from Product p where p.productCode = ?1")
    Optional<Product> findByProductCode(String productCode);

    @Query(value = "select count(id)> 0 from contract_item where product_id = :productCode", nativeQuery = true)
    boolean hasAssociatedContractItems(Long productCode);

    @Query(value = "select count(id)> 0 from warehouse_receipt_item where product_id = :productCode", nativeQuery = true)
    boolean hasAssociatedWarehouseReceiptItems(Long productCode);
}