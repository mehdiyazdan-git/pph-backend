package com.armaninvestment.parsparandreporter.repositories;

import com.armaninvestment.parsparandreporter.entities.WarehouseReceiptItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WarehouseReceiptItemRepository extends JpaRepository<WarehouseReceiptItem, Long> {


    @Query(value = """
            SELECT
             cast(coalesce(sum(wri.quantity * wri.unit_price), 0) as double precision) as total_quantity_not_invoiced, \s
             cast(coalesce(sum(wri.quantity), 0) as double precision) as total_quantity_not_invoiced \s
                            FROM warehouse_receipt wr\s
                            JOIN warehouse_receipt_item wri on wr.id = wri.warehouse_receipt_id\s
                            JOIN public.report_item ri ON wr.id = ri.warehouse_receipt_id\s
                            LEFT JOIN public.invoice_item ii ON wr.id = ii.warehouse_receipt_id\s
                            JOIN year y  on y.id = wr.year_id\s
                            WHERE ii.invoice_id IS NULL\s
                            AND wr.customer_id = :customerId
            """, nativeQuery = true)
    List<Object[]> getNotInvoicedTotalAmountAndTotalQuantityByCustomerId(Long customerId);

    @Query(value = """
            select wr.warehouse_receipt_number,
                   wr.warehouse_receipt_date,
                   wr.warehouse_receipt_description,
                   cast(coalesce(sum(wri.quantity * wri.unit_price), 0) as bigint) as total_quantity_not_invoiced
                    from warehouse_receipt wr
                    join warehouse_receipt_item wri on wr.id = wri.warehouse_receipt_id
                     join report_item ri on wr.id = ri.warehouse_receipt_id
                     left join invoice_item ii on wr.id = ii.warehouse_receipt_id
                    left join year y on y.id = wr.year_id
            where wr.customer_id = :customerId
            and ii.id is null
            and y.name = :yearName
            group by wr.warehouse_receipt_number,
                   wr.warehouse_receipt_date,
                   wr.warehouse_receipt_description
            """, nativeQuery = true)
    List<Object[]> getNotInvoicedAmountDetailsByCustomerIdAndYearName(@Param("customerId") Long customerId, @Param("yearName") Long yearName);


    @Query(value = "select cast(coalesce(sum(quantity * unit_price),0) as bigint) from returned where customer_id = :customerId", nativeQuery = true)
    Object getReturnedByCustomerId(Long customerId);

    @Query(value = """
            select coalesce(sum(case
                   when adjustment_type = 'POSITIVE' THEN a.unit_price * a.quantity
                   when adjustment_type = 'NEGATIVE' THEN (a.unit_price * a.quantity) * -1
                   ELSE 0
                   END),0)
            from adjustment a left join invoice i on i.id = a.invoice_id
            where i.customer_id = :customerId
            """, nativeQuery = true)
    Object getAdjustmentsByCustomerId(Long customerId);

    @Query(value = """
            with deposits as (
                select case
                           when i.sales_type = 'CONTRACTUAL_SALES' then c2.contract_number
                           when i.sales_type = 'CASH_SALES' then 'نقدی'
                           end                  as contract_number,
                       cast(coalesce(sum(I.advanced_payment),0) as double precision)  AS advanced_payment,
                       cast(coalesce(sum(I.performance_bound),0) as double precision) AS performance_bound,
                       cast(coalesce(sum(I.insurance_deposit),0) as double precision) AS insurance_deposit
                from invoice i
                         left join contracts c2 on c2.id = i.contract_id
                         join customer c on c.id = i.customer_id
                where i.customer_id = :customerId
                group by contract_number, i.sales_type
            ),
                sales_amount as (
                    select case
                               when i.sales_type = 'CONTRACTUAL_SALES' then c2.contract_number
                               when i.sales_type = 'CASH_SALES' then 'نقدی'
                               end as contract_number,
                           cast(coalesce(sum(ii.unit_price * ii.quantity),0) as double precision) as sales_amount,
                           cast(coalesce(sum(ii.quantity),0) as double precision) as sales_quantity,
                           cast(round(coalesce(sum(ii.unit_price * ii.quantity),0) * 0.09) as double precision) as vat
                    from invoice i
                             join invoice_item ii on i.id = ii.invoice_id
                             left join contracts c2 on c2.id = i.contract_id
                             join customer c on c.id = i.customer_id
                    where i.customer_id = :customerId
                    group by contract_number, i.sales_type
                )
            select coalesce(d.contract_number,s.contract_number),
                   coalesce(advanced_payment,0),
                   coalesce(performance_bound,0),
                   coalesce(insurance_deposit,0),
                   sales_amount,
                   sales_quantity,
                   vat
            from deposits d right outer join sales_amount s on d.contract_number = s.contract_number
            """, nativeQuery = true)
    List<Object[]> getClientSummaryByCustomerId(Long customerId);

    @Query(nativeQuery = true, value =
            "SELECT * " +
                    "FROM (SELECT COALESCE(c.contract_number, 'نقدی') AS contract_number, " +
                    "cast(i.id as bigint), " +
                    "i.invoice_number, " +
                    "gregorian_to_persian(i.issued_date + INTERVAL '1 DAY') As issued_date, " +
                    "SUM(ii.quantity) AS total_quantity, " +
                    "CAST(SUM(ii.unit_price * ii.quantity) AS bigint) AS total_amount, " +
                    "coalesce(i.advanced_payment,0), " +
                    "coalesce(i.insurance_deposit,0), " +
                    "coalesce(i.performance_bound,0) " +  // Include the new columns
                    "FROM invoice i " +
                    "JOIN invoice_item ii ON i.id = ii.invoice_id " +
                    "LEFT JOIN customer c2 ON c2.id = i.customer_id " +
                    "LEFT JOIN contracts c ON c.id = i.contract_id " +
                    "WHERE c2.id = :customerId " +
                    "GROUP BY i.id, i.invoice_number, COALESCE(c.contract_number, 'نقدی'), gregorian_to_persian(i.issued_date)) AS iic2c " +
                    "WHERE iic2c.contract_number = :contractNumber ORDER BY IIC2C.issued_date")
    List<Object[]> getClientSummaryDetailsByContractNumber(@Param("customerId") Long customerId, @Param("contractNumber") String contractNumber);

}