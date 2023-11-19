package com.armaninvestment.parsparandreporter.repositories;

import com.armaninvestment.parsparandreporter.dtos.NotInvoiced;
import com.armaninvestment.parsparandreporter.entities.WarehouseReceipt;
import com.armaninvestment.parsparandreporter.entities.Year;
import org.springframework.data.jpa.repository.JpaRepository;
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

    @Query("SELECT wr FROM WarehouseReceipt wr WHERE wr.warehouseReceiptNumber = :number AND wr.warehouseReceiptDate = :date")
    WarehouseReceipt findByNumberAndDate(@Param("number") Long number, @Param("date") LocalDate date);

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

    @Query(value = "select id,description from get_all_warehouse_receipts_by_receipt_number_and_year_name(:yearId)", nativeQuery = true)
    List<Object[]> getWareHouseReceipts(@Param("yearId") Long yearId);

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

}