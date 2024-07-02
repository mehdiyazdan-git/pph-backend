CREATE VIEW customer_report_view AS
SELECT c.id                                                                                              AS customer_id,
       c.name                                                                                            AS customer_name,
       c.monthly_report                                                                                  AS big_customer,
       p.product_code                                                                                    AS product_code,
       get_persian_year(gregorian_to_persian(r.report_date + INTERVAL '1 DAY'))                          AS year,
       get_persian_month(gregorian_to_persian(r.report_date + INTERVAL '1 DAY'))                         AS month,
       get_persian_month_name(get_persian_month(gregorian_to_persian(r.report_date + INTERVAL '1 DAY'))) AS month_name,
       sum(ri.quantity * ri.unit_price)                                                                  AS total_amount,
       sum(ri.quantity)                                                                                  AS total_quantity,
       round(avg(ri.unit_price))                                                                         AS avg_unit_price
FROM report_item ri
         JOIN report r ON r.id = ri.report_id
         JOIN customer c ON c.id = ri.customer_id
         JOIN warehouse_receipt wr ON wr.id = ri.warehouse_receipt_id
         JOIN warehouse_receipt_item wri ON wr.id = wri.warehouse_receipt_id
         JOIN product p ON p.id = wri.product_id
GROUP BY c.id, c.name, c.monthly_report, year, month, month_name, product_code;