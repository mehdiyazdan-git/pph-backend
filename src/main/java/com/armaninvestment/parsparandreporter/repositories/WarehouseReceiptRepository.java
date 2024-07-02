package com.armaninvestment.parsparandreporter.repositories;

import com.armaninvestment.parsparandreporter.dtos.NotInvoiced;
import com.armaninvestment.parsparandreporter.entities.WarehouseReceipt;
import com.armaninvestment.parsparandreporter.entities.Year;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public interface WarehouseReceiptRepository extends JpaRepository<WarehouseReceipt, Long> {

    @Query(value = "SELECT is_warehouse_receipt_exist_by_id(:warehouseReceiptId)", nativeQuery = true)
    boolean isWarehouseReceiptExistById(Long warehouseReceiptId);

    @Query(value = "SELECT is_warehouse_receipt_duplicate(:warehouseReceiptId, :currentInvoiceItemId)", nativeQuery = true)
    boolean isDuplicateWarehouseReceipt(Long warehouseReceiptId, Long currentInvoiceItemId);

    @Query(value = "SELECT is_warehouse_receipt_is_duplicate_for_report_item(:warehouseReceiptId, :currentReportItemId)", nativeQuery = true)
    boolean isWarehouseReceiptIsDuplicateForReportItem(Long warehouseReceiptId, Long currentReportItemId);

    @Query(value = "SELECT get_warehouse_receipt_id_by_warehouse_receipt_number_and_date(:receiptNumber, :receiptDate)", nativeQuery = true)
    Long getWarehouseReceiptIdByWarehouseReceiptNumberAndDate(Long receiptNumber, LocalDate receiptDate);

    @Query("""
            select w from WarehouseReceipt w
            where w.warehouseReceiptNumber = :warehouseReceiptNumber
            order by w.warehouseReceiptNumber""")
    List<WarehouseReceipt> findByWarehouseReceiptNumberOrderByWarehouseReceiptNumberAsc(@Param("warehouseReceiptNumber") Long warehouseReceiptNumber);

    @Query("""
            select w from WarehouseReceipt w
            where w.warehouseReceiptNumber = :warehouseReceiptNumber
            order by w.warehouseReceiptNumber""")
    Optional<WarehouseReceipt> findByReceiptNumber(@Param("warehouseReceiptNumber") Long warehouseReceiptNumber);

    @Query(value = "select id from warehouse_receipt w where warehouse_receipt_number = :warehouseReceiptNumber", nativeQuery = true)
    Long findWarehouseReceiptByWarehouseReceiptNumber(@Param("warehouseReceiptNumber") Long warehouseReceiptNumber);

    @Query("""
            select w from WarehouseReceipt w
            where w.warehouseReceiptNumber = :warehouseReceiptNumber and w.warehouseReceiptDate = :warehouseReceiptDate""")
    WarehouseReceipt findWarehouseReceiptByWarehouseReceiptNumberAndWarehouseReceiptDate(@Param("warehouseReceiptNumber") Long warehouseReceiptNumber, @Param("warehouseReceiptDate") LocalDate warehouseReceiptDate);

    @Query("""
            select (count(w) > 0) from WarehouseReceipt w
            where w.warehouseReceiptNumber = :warehouseReceiptNumber and w.warehouseReceiptDate = :warehouseReceiptDate""")
    boolean existsWarehouseReceiptByWarehouseReceiptNumberAndWarehouseReceiptDate(@Param("warehouseReceiptNumber") Long warehouseReceiptNumber, @Param("warehouseReceiptDate") LocalDate warehouseReceiptDate);


    @Query("""
            select (count(w) > 0) from WarehouseReceipt w
            where w.warehouseReceiptNumber = :warehouseReceiptNumber and w.year = :year""")
    boolean existsWarehouseReceiptByWarehouseReceiptNumberAndYear(@Param("warehouseReceiptNumber") Long warehouseReceiptNumber, @Param("year") Year year);

    @Query("select w from WarehouseReceipt w where w.year = :year order by w.warehouseReceiptDate")
    List<WarehouseReceipt> findAllByYearOrderByWarehouseReceiptDate(@Param("year") Year year);

    @Query(value = "select * from get_all_warehouse_receipts_by_year_name(:yearName)", nativeQuery = true)
    List<Object[]> getAllWarehouseReceiptsByYearName(@Param("yearName") Long yearName);

    @Query(value = "select id,description from get_all_warehouse_receipts_by_receipt_number_and_year_name(:yearId)", nativeQuery = true)
    List<Object[]> getAllWarehouseReceiptsByReceiptNumberAndYearName(@Param("yearId") Long yearId);

    @Query(value = "select id,description from search_warehouse_receipt_by_description_keywords(:searchQuery,:yearId)", nativeQuery = true)
    List<Object[]> searchWarehouseReceiptByDescriptionKeywords(@Param("searchQuery") String searchQuery, @Param("yearId") Long yearId);


    @Query("""
            select w from WarehouseReceipt w
            where w.warehouseReceiptNumber = :warehouseReceiptNumber and w.year = :year
            order by w.warehouseReceiptNumber""")
    List<WarehouseReceipt> findByWarehouseReceiptNumberAndYearOrderByWarehouseReceiptNumberAsc(@Param("warehouseReceiptNumber") Long warehouseReceiptNumber, @Param("year") Year year);


    @Query("select w from WarehouseReceipt w where w.year = :year order by w.warehouseReceiptDate")
    List<WarehouseReceipt> findAllByYearOrderByWarehouseReceiptDateAsc(@Param("year") Year year);

    @Query(nativeQuery = true, value = "SELECT * FROM invoiced_not_invoiced_by_customer_code_and_year_name(" +
            "cast(:customerCode AS varchar)," +
            " cast(:yearName AS bigint)," +
            "cast(:invoiced AS boolean))")
    List<Object[]> findNotInvoicedByYearAndCustomer(String customerCode, Long yearName, Boolean invoiced);


    default List<NotInvoiced> mapToNotInvoicedList(String customerCode, Long yearName, Boolean invoiced) {
        List<Object[]> results = findNotInvoicedByYearAndCustomer(customerCode, yearName, invoiced);
        List<NotInvoiced> notInvoicedList = new ArrayList<>();

        for (Object[] result : results) {
            notInvoicedList.add(new NotInvoiced(
                    (Long) result[0],      // id
                    (Long) result[1],      // receiptNo
                    (String) result[2],   // description
                    (String) result[3],   // receiptDate
                    (String) result[4],   // customerCode
                    (String) result[5],   // customerName
                    (BigDecimal) result[6],      // totalAmount
                    (Long) result[7],      // totalQuantity
                    (Long) result[8]       // itemCount
            ));
        }

        return notInvoicedList;
    }

    @Query(value = "select * from get_warehouse_receipt_by_id(:warehouseReceiptId)", nativeQuery = true)
    List<Object[]> getWarehouseReceiptById(@Param("warehouseReceiptId") Long warehouseReceiptId);

    @Query(value = "select * from get_warehouse_receipt_items_by_id(:warehouseReceiptItemId)", nativeQuery = true)
    List<Object[]> getWarehouseReceiptItemsById(@Param("warehouseReceiptItemId") Long warehouseReceiptItemId);


    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "INSERT INTO public.warehouse_receipt_item " +
            "(quantity, unit_price, product_id, warehouse_receipt_id) " +
            "VALUES (:quantity, :unitPrice, :productId, :warehouseReceiptId)")
    void insertWareHouseReceiptItem(
            @Param("productId") Long productId,
            @Param("quantity") Integer quantity,
            @Param("unitPrice") Long unitPrice,
            @Param("warehouseReceiptId") Long warehouseReceiptId);


    @Transactional
    @Modifying
    @Query(value = "delete from public.warehouse_receipt_item wri where wri.warehouse_receipt_id = :warehouseReceiptId", nativeQuery = true)
    void deleteWareHouseReceiptItems(Long warehouseReceiptId);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "UPDATE public.warehouse_receipt SET " +
            "warehouse_receipt_date = :warehouseReceiptDate, " +
            "warehouse_receipt_description = :warehouseReceiptDescription, " +
            "warehouse_receipt_number = :warehouseReceiptNumber, " +
            "customer_id = :customerId, " +
            "year_id = :yearId " +
            "WHERE id = :id")
    void updateWareHouseReceiptById(@Param("warehouseReceiptDate") LocalDate warehouseReceiptDate,
                                    @Param("warehouseReceiptDescription") String warehouseReceiptDescription,
                                    @Param("warehouseReceiptNumber") Long warehouseReceiptNumber,
                                    @Param("customerId") Long customerId,
                                    @Param("yearId") Long yearId,
                                    @Param("id") Long id);


}