--1
create
or replace function get_all_reports_by_year_id(p_year_id bigint)
    returns TABLE(reportid bigint, date date, totalamount numeric, totalquantity bigint)
    language plpgsql
as
$$
BEGIN
RETURN QUERY
SELECT r.id As reportId,
       r.report_date::date As date,
            sum(ri.unit_price * ri.quantity) As totalAmount,
            sum(ri.quantity) As totalQuantity
FROM report r
    join report_item ri
ON r.id = ri.report_id
WHERE r.year_id = p_year_id
GROUP BY r.id, r.report_date:: date;
END;
$$;

alter function get_all_reports_by_year_id(bigint) owner to postgres;
--2
create
or replace function get_contract_and_invoice_totals(given_contract_id bigint, given_invoice_number bigint)
    returns TABLE(cumulative_contract_amount bigint, cumulative_contract_quantity bigint, contract_advanced_payment bigint, contract_performance_bond bigint, contract_insurance_deposit bigint, total_committed_contract_amount bigint, total_remaining_contract_amount bigint, total_committed_contract_count bigint, total_invoice_count bigint, total_consumed_contract_advanced_payment bigint, total_outstanding_contract_advanced_payment bigint, total_committed_performance_bond bigint, total_remaining_performance_bond bigint, total_committed_insurance_deposit bigint, total_remaining_insurance_deposit bigint)
    language plpgsql
as
$$

BEGIN
RETURN QUERY WITH ContractTotals AS (
            SELECT
                SUM(ci.unit_price * ci.quantity) AS total_contract_amount,
                SUM(ci.quantity) AS total_contract_count,
                SUM(ci.unit_price * ci.quantity) * c.advance_payment AS advanced_payment,
                SUM(ci.unit_price * ci.quantity) * c.performance_bond AS performance_bond,
                SUM(ci.unit_price * ci.quantity) * c.insurance_deposit AS insurance_deposit
            FROM
                contract_item ci
                    JOIN
                contracts c ON c.id = ci.contract_id
            WHERE
                    ci.contract_id = given_contract_id
            GROUP BY
                c.advance_payment, c.performance_bond, c.insurance_deposit
        ),
             InvoiceTotals AS (
                 SELECT
                     SUM(ii.unit_price * ii.quantity) AS total_invoice_amount,
                     SUM(ii.quantity) AS total_invoice_count,
                     SUM(ii.unit_price * ii.quantity) * c.advance_payment AS advanced_payment,
                     SUM(ii.unit_price * ii.quantity) * c.performance_bond AS performance_bond,
                     SUM(ii.unit_price * ii.quantity) * c.insurance_deposit AS insurance_deposit
                 FROM
                     invoice_item ii
                         JOIN
                     invoice i ON i.id = ii.invoice_id
                         JOIN
                     contracts c ON c.id = i.contract_id
                 WHERE i.contract_id = given_contract_id
                   AND (given_invoice_number IS NULL OR i.id < given_invoice_number)
                 GROUP BY
                     c.advance_payment, c.performance_bond, c.insurance_deposit
             )

-- Combine the results
SELECT CAST(COALESCE(ContractTotals.total_contract_amount, 0) AS bigint)                                      AS cumulative_contract_amount,
       CAST(COALESCE(ContractTotals.total_contract_count, 0) AS bigint)                                       AS cumulative_contract_quantity,
       CAST(COALESCE(ContractTotals.advanced_payment, 0) AS bigint)                                           AS contract_advanced_payment,
       CAST(COALESCE(ContractTotals.performance_bond, 0) AS bigint)                                           AS contract_performance_bond,
       CAST(COALESCE(ContractTotals.insurance_deposit, 0) AS bigint)                                          AS contract_insurance_deposit,
       CAST(COALESCE(InvoiceTotals.total_invoice_amount, 0) AS bigint)                                        AS total_committed_contract_amount,
       CAST(COALESCE(ContractTotals.total_contract_amount - InvoiceTotals.total_invoice_amount,
                     0) AS bigint)                                                                            AS total_remaining_contract_amount,
       CAST(COALESCE(InvoiceTotals.total_invoice_count, 0) AS bigint)                                         AS total_committed_contract_count,
       CAST(COALESCE(ContractTotals.total_contract_count - InvoiceTotals.total_invoice_count,
                     0) AS bigint)                                                                            AS total_invoice_count,
       CAST(COALESCE(InvoiceTotals.advanced_payment, 0) AS bigint)                                            AS total_consumed_contract_advanced_payment,
       CAST(COALESCE(ContractTotals.advanced_payment - InvoiceTotals.advanced_payment,
                     0) AS bigint)                                                                            AS total_outstanding_contract_advanced_payment,
       CAST(COALESCE(InvoiceTotals.performance_bond, 0) AS bigint)                                            AS total_commited_performance_bond,
       CAST(COALESCE(ContractTotals.performance_bond - InvoiceTotals.performance_bond,
                     0) AS bigint)                                                                            AS total_remaining_performance_bond,
       CAST(COALESCE(InvoiceTotals.insurance_deposit, 0) AS bigint)                                           AS total_commited_insurance_deposit,
       CAST(COALESCE(ContractTotals.insurance_deposit - InvoiceTotals.insurance_deposit,
                     0) AS bigint)                                                                            AS total_remaining_insurance_deposit
FROM ContractTotals
         FULL OUTER JOIN
     InvoiceTotals ON 1 = 1;

END;
$$;

alter function get_contract_and_invoice_totals(bigint, bigint) owner to postgres;
--3
create
or replace function get_customer_invoices_by_year_and_customer_code(p_customer_code text, p_year_name bigint)
    returns TABLE(id bigint, invoicenumber bigint, issueddate character varying, total_amount numeric, total_quantity bigint)
    language plpgsql
as
$$
BEGIN
RETURN QUERY
SELECT i.id,
       i.invoice_number                    As invoiceNumber,
       gregorian_to_persian(i.issued_date) AS issuedDate,
       SUM(ii.unit_price * ii.quantity)    AS total_amount,
       SUM(ii.quantity)                    AS total_quantity
FROM invoice_item ii
         JOIN invoice i ON i.id = ii.invoice_id
         JOIN contracts c on c.id = i.contract_id
         JOIN customer c2 on c2.id = c.customer_id
WHERE c2.customer_code = p_customer_code
  AND get_persian_year(gregorian_to_persian(i.issued_date)) = p_year_name
GROUP BY i.id, i.invoice_number, i.issued_date;
END;
$$;

alter function get_customer_invoices_by_year_and_customer_code(text, bigint) owner to postgres;
--4
create
or replace function get_customer_reports_by_year_and_customer_code(p_customer_code text, p_year_name bigint)
    returns TABLE(report_date_persian character varying, explanation character varying, total_amount numeric, total_quantity bigint)
    language plpgsql
as
$$
BEGIN
RETURN QUERY
SELECT gregorian_to_persian(r.report_date) AS report_date_persian,
       r.report_explanation                AS explanation,
       SUM(ri.unit_price * ri.quantity)    AS total_amount,
       SUM(ri.quantity)                    AS total_quantity
FROM report_item ri
         JOIN report r ON r.id = ri.report_id
         JOIN customer c on c.id = ri.customer_id
WHERE c.customer_code = p_customer_code
  AND get_persian_year(gregorian_to_persian(r.report_date)) = p_year_name
GROUP BY r.report_date, r.report_explanation;
END;
$$;

alter function get_customer_reports_by_year_and_customer_code(text, bigint) owner to postgres;
--5
create
or replace function get_invoice_count_for_contract(contractid bigint) returns integer
    language plpgsql
as
$$
DECLARE
invoice_count integer;
BEGIN
SELECT COUNT(id)
INTO invoice_count
FROM Invoice
WHERE contract_id = contractId;

RETURN invoice_count;
END;
$$;

alter function get_invoice_count_for_contract(bigint) owner to postgres;
--6
create
or replace function get_monthly_report_by_year_and_month(p_year integer, p_month integer, p_product_type text)
    returns TABLE(id bigint, name character varying, big_customer boolean, quantity bigint, avg_unit_price numeric, amount numeric, cumulative_quantity bigint)
    language plpgsql
as
$$
BEGIN
RETURN QUERY
SELECT c.id,
       c.name,
       c.monthly_report,
       SUM(ri.quantity)                                                           AS quantity,
       ROUND(AVG(ri.unit_price), 0)                                               AS avg_unit_price,
       SUM(ri.quantity * ri.unit_price)                                           AS amount,
       (SELECT SUM(ri2.quantity)
        FROM report_item ri2
                 JOIN report r2 ON r2.id = ri2.report_id
        WHERE ri2.customer_id = c.id
          AND get_persian_year(gregorian_to_persian(r2.report_date)) = p_year
          AND get_persian_month(gregorian_to_persian(r2.report_date)) <= p_month) AS cumulative_quantity
FROM customer c
         JOIN report_item ri ON c.id = ri.customer_id
         JOIN warehouse_receipt on ri.warehouse_receipt_id = warehouse_receipt.id
         JOIN warehouse_receipt_item wi on warehouse_receipt.id = wi.warehouse_receipt_id
         JOIN product p on p.id = wi.product_id
         JOIN report r ON r.id = ri.report_id
WHERE get_persian_year(gregorian_to_persian(r.report_date)) = p_year
  AND get_persian_month(gregorian_to_persian(r.report_date)) = p_month
  AND LEFT (p.product_code
    , 1) = p_product_type
GROUP BY c.id, c.name;
END;
$$;

alter function get_monthly_report_by_year_and_month(integer, integer, text) owner to postgres;
--7
create
or replace function get_payments_by_customer_and_year_group_by_month(customer_id_param bigint, year smallint)
    returns TABLE(month_number smallint, month_name text, total_amount double precision)
    language plpgsql
as
$$
BEGIN
RETURN QUERY
SELECT get_persian_month(gregorian_to_persian(payment_date))                         as month_number,
       get_persian_month_name(get_persian_month(gregorian_to_persian(payment_date))) as month_name,
       sum(payment_amount)                                                           as total_amount
FROM payment
WHERE customer_id = $1
  AND get_persian_year(gregorian_to_persian(payment_date)) = $2
group by month_number, month_name;
END;
$$;

alter function get_payments_by_customer_and_year_group_by_month(bigint, smallint) owner to postgres;
--8
create
or replace function get_payments_by_customer_code_and_year_name(p_customer_code character varying, p_year_name bigint)
    returns TABLE(payment_date_persian character varying, paymentamount double precision)
    language plpgsql
as
$$
BEGIN
RETURN QUERY
SELECT gregorian_to_persian(payment_date) AS payment_date_persian,
       payment_amount                     As paymentAmount
FROM payment
         JOIN customer c ON c.id = payment.customer_id
WHERE c.customer_code = p_customer_code
  AND get_persian_year(gregorian_to_persian(payment_date)) = p_year_name;
END;
$$;

alter function get_payments_by_customer_code_and_year_name(varchar, bigint) owner to postgres;
--9
create
or replace function get_payments_by_year_group_by_month(year smallint)
    returns TABLE(month_number smallint, month_name text, total_amount double precision)
    language plpgsql
as
$$
BEGIN
RETURN QUERY
SELECT get_persian_month(gregorian_to_persian(payment_date))                         as month_number,
       get_persian_month_name(get_persian_month(gregorian_to_persian(payment_date))) as month_name,
       sum(payment_amount)                                                           as total_amount
FROM payment
WHERE get_persian_year(gregorian_to_persian(payment_date)) = $1
group by month_number, month_name;
END;
$$;

alter function get_payments_by_year_group_by_month(smallint) owner to postgres;
--10
create
or replace function get_persian_day(persian_date character varying) returns smallint
    language plpgsql
as
$$
DECLARE
day_part character varying(2);
BEGIN
    day_part
:= substring(persian_date from 9 for 2);
RETURN CAST(day_part AS smallint);
END;
$$;

alter function get_persian_day(varchar) owner to postgres;
--11
create
or replace function get_persian_month(persian_date character varying) returns smallint
    language plpgsql
as
$$
DECLARE
month_part character varying(2);
BEGIN
    month_part
:= substring(persian_date from 6 for 2);
RETURN CAST(month_part AS smallint);
END;
$$;

alter function get_persian_month(varchar) owner to postgres;
--12
create
or replace function get_persian_month_name(persian_month integer) returns text
    language plpgsql
as
$$
BEGIN
RETURN CASE persian_month
           WHEN 1 THEN 'فروردین'
           WHEN 2 THEN 'اردیبهشت'
           WHEN 3 THEN 'خرداد'
           WHEN 4 THEN 'تیر'
           WHEN 5 THEN 'مرداد'
           WHEN 6 THEN 'شهریور'
           WHEN 7 THEN 'مهر'
           WHEN 8 THEN 'آبان'
           WHEN 9 THEN 'آذر'
           WHEN 10 THEN 'دی'
           WHEN 11 THEN 'بهمن'
           WHEN 12 THEN 'اسفند'
           ELSE ''
    END;
END;
$$;

alter function get_persian_month_name(integer) owner to postgres;
--13
create
or replace function get_persian_year(persian_date character varying) returns smallint
    language plpgsql
as
$$
DECLARE
year_part character varying(4);
BEGIN
    year_part
:= substring(persian_date from 1 for 4);
RETURN CAST(year_part AS smallint);
END;
$$;

alter function get_persian_year(varchar) owner to postgres;
--14
create
or replace function get_sales_by_customer_and_year_group_by_month(customer_id bigint, year smallint)
    returns TABLE(month_number smallint, month_name text, total_amount numeric, total_count bigint)
    language plpgsql
as
$$
BEGIN
RETURN QUERY
SELECT get_persian_month(gregorian_to_persian(r.report_date))                         as month_number,
       get_persian_month_name(get_persian_month(gregorian_to_persian(r.report_date))) as month_name,
       sum(s.unit_price * s.quantity)                                                 as total_amount,
       sum(s.quantity)                                                                as total_count
FROM report_item as s
         join report r on r.id = s.report_id
WHERE s.customer_id = $1
  AND get_persian_year(gregorian_to_persian(r.report_date)) = $2
group by month_number, month_name;
END;
$$;

alter function get_sales_by_customer_and_year_group_by_month(bigint, smallint) owner to postgres;
--15
create
or replace function  get_sales_by_year_group_by_month(year smallint)
    returns TABLE(month_number smallint, month_name text, total_amount numeric, total_quantity bigint)
    language plpgsql
as
$$
BEGIN
RETURN QUERY
SELECT get_persian_month(gregorian_to_persian(r.report_date))                         as month_number,
       get_persian_month_name(get_persian_month(gregorian_to_persian(r.report_date))) as month_name,
       sum(s.unit_price * s.quantity)                                                 as total_amount,
       sum(s.quantity)                                                                as total_quantity
FROM report_item as s
         join report r on r.id = s.report_id
WHERE get_persian_year(gregorian_to_persian(r.report_date)) = $1
group by month_number, month_name;
END;
$$;

alter function get_sales_by_year_group_by_month(smallint) owner to postgres;
--16
create
or replace function get_sales_by_year_group_by_month_filter_by_product_type(p_year smallint, p_product_type text)
    returns TABLE(month_number smallint, month_name text, total_amount numeric, total_quantity bigint)
    language plpgsql
as
$$
BEGIN
RETURN QUERY
SELECT get_persian_month(gregorian_to_persian(r.report_date))                         AS month_number,
       get_persian_month_name(get_persian_month(gregorian_to_persian(r.report_date))) AS month_name,
       sum(s.unit_price * s.quantity)                                                 AS total_amount,
       sum(s.quantity)                                                                AS total_quantity
FROM report_item AS s
         JOIN warehouse_receipt wr ON wr.id = s.warehouse_receipt_id
         JOIN warehouse_receipt_item wri ON wr.id = wri.warehouse_receipt_id
         JOIN product p ON p.id = wri.product_id
         JOIN report r ON r.id = s.report_id
WHERE get_persian_year(gregorian_to_persian(r.report_date)) = p_year
  AND LEFT (p.product_code
    , 1) = p_product_type
GROUP BY month_number, month_name;
END;
$$;

alter function get_sales_by_year_group_by_month_filter_by_product_type(smallint, text) owner to postgres;
--17
create
or replace function gregorian_to_persian(in_date timestamp with time zone) returns character varying
    language plpgsql
as
$$
DECLARE
y smallint;
    aday
smallint;
    amonth
smallint;
    ayear
smallint;
value smallint;
    a1
char(4);
    b1
char(2);
    c1
char(2);
    Tday
smallint;
    Tmonth
smallint;
    Tyear
smallint;
    temp
smallint;
    CabisehYear
smallint;
    TMonthEnd
smallint;
    numdays
int;
    now_day
timestamp without time zone;
    a
timestamp without time zone;
    Const_Date
timestamp without time zone;
BEGIN
    Const_Date
= cast('3/21/1921' as timestamp without time zone);
    numdays
= DATE_PART('day',in_date - Const_Date);
    aday
= 1;
    amonth
= 1;
    ayear
= 1300;
    CabisehYear
=cast((numdays / 1461) as int);
    numdays
= numdays - CabisehYear * 1461;
    Tyear
= cast((numdays / 365) as int);
    If
Tyear = 4 then
        Tyear = Tyear - 1;
end if;
    numdays
= numdays - Tyear * 365;
    Tmonth
=cast((numdays / 31) as int);
    If
(Tmonth > 6) then
        Tmonth = 6;
end if;
    numdays
= numdays - Tmonth * 31;
    TMonthEnd
= 0;
    If
(numdays >= 30 And Tmonth = 6 ) then
        TMonthEnd =cast((numdays / 30) as int);
        If
TMonthEnd >= 5 then
            TMonthEnd = 5;
end if;
        numdays
= numdays - TMonthEnd * 30;
End if;
    Tmonth
= (TMonthEnd + Tmonth);
    Tday
= numdays;
    Tyear
= (Tyear + CabisehYear * 4);
    ayear
= (ayear + Tyear);
    amonth
= amonth + Tmonth;
    aday
= aday + Tday;

    a1
= ayear;
    b1
= amonth;
    c1
= aday;


    If
length(b1) = 1 then
        b1 = '0' || b1;
end if;
    If
length(c1) = 1 then
        c1 = '0' || c1;
end if;
return a1 || '/' || b1 || '/' || c1;
END;
$$;

alter function gregorian_to_persian(timestamp with time zone) owner to postgres;
--18
create
or replace function invoiced_not_invoiced_by_customer_code_and_year_name(p_customer_code character varying, p_year_name bigint, p_invoiced boolean)
    returns TABLE(id bigint, receiptno bigint, description character varying, receiptdate character varying, customer_code character varying, customer_name character varying, total_amount numeric, total_quantity bigint, item_count bigint)
    language plpgsql
as
$$
BEGIN
RETURN QUERY
SELECT wr.id,
       wr.warehouse_receipt_number                     AS receiptNo,
       wr.warehouse_receipt_description                AS description,
       gregorian_to_persian(wr.warehouse_receipt_date) AS receiptDate,
       c.customer_code                                 AS customerCode,
       c.name                                          AS customer_name,
       SUM(wi.unit_price * wi.quantity)                AS total_amount,
       SUM(wi.quantity)                                AS total_quantity,
       COUNT(wi.id)                                    AS item_count
FROM warehouse_receipt_item wi
         JOIN warehouse_receipt wr ON wr.id = wi.warehouse_receipt_id
         JOIN year y ON y.id = wr.year_id
         JOIN customer c ON c.id = wr.customer_id
         LEFT OUTER JOIN invoice_item ri ON wi.warehouse_receipt_id = ri.warehouse_receipt_id
WHERE (p_invoiced IS NULL OR (p_invoiced = true AND ri.id IS NOT NULL) OR (p_invoiced = false AND ri.id IS NULL))
  AND (p_customer_code IS NULL OR c.customer_code = p_customer_code)
  AND (p_year_name IS NULL OR y.name = p_year_name)
GROUP BY wr.id, receiptNo, description, wr.warehouse_receipt_date, c.customer_code, customer_name;

RETURN;
END;
$$;

alter function invoiced_not_invoiced_by_customer_code_and_year_name(varchar, bigint, boolean) owner to postgres;
--19
create
or replace function monthly_sales_by_month_and_year(month_number integer, year integer)
    returns TABLE(customerid bigint, name character varying, total_amount numeric, total_count bigint, avg_price numeric)
    language plpgsql
as
$$
DECLARE
customerId bigint;
BEGIN
RETURN QUERY SELECT c.id, c.name, sum(ri.unit_price * ri.quantity) as total_amount, sum(ri.quantity) as total_count, round(avg(ri.unit_price)) as avg_price
                 FROM customer c
                          JOIN report_item ri ON c.id = ri.customer_id
                          JOIN report r ON r.id = ri.report_id
                 WHERE get_persian_year(gregorian_to_persian(r.report_date)) = year AND get_persian_month(gregorian_to_persian(r.report_date)) = month_number
                 GROUP BY c.id, c.name;
END;
$$;

alter function monthly_sales_by_month_and_year(integer, integer) owner to postgres;
--20
create
or replace function search_warehouse_receipt_by_description_keywords(search_term text, fiscal_year_id bigint)
    returns TABLE(id bigint, description text)
    language plpgsql
as
$$
BEGIN
RETURN QUERY
SELECT i.id, i.description
FROM (SELECT wwc.id,
             CONCAT('حواله ', wwc.receiptNo, '-', wwc.customerName, ' تاریخ ', wwc.receiptDate, ' تعداد',
                    wwc.quantity) AS description
      FROM (SELECT wr.id,
                   wr.warehouse_receipt_number                     AS receiptNo,
                   gregorian_to_persian(wr.warehouse_receipt_date) AS receiptDate,
                   c.name                                          AS customerName,
                   SUM(wri.quantity)                               AS quantity
            FROM warehouse_receipt wr
                     JOIN warehouse_receipt_item wri ON wr.id = wri.warehouse_receipt_id
                     JOIN customer c ON c.id = wr.customer_id
            WHERE wr.year_id = fiscal_year_id
            GROUP BY wr.id, receiptNo, receiptDate, customerName) wwc) i
WHERE i.description ILIKE '%' || search_term || '%';

RETURN;
END;
$$;

alter function search_warehouse_receipt_by_description_keywords(text, bigint) owner to postgres;
--21
create
or replace function search_warehouse_receipts_by_receiptnumber_and_year_name(searchQuery character varying, year bigint)
    returns TABLE(id bigint, description text)
    language plpgsql
as
$$
BEGIN
RETURN QUERY
SELECT wwc.id,
       CONCAT('حواله ', wwc.receiptNo, '-', wwc.customerName, ' تاریخ ', wwc.receiptDate, ' تعداد',
              wwc.quantity) AS description
FROM (SELECT wr.id,
             wr.warehouse_receipt_number                     AS receiptNo,
             gregorian_to_persian(wr.warehouse_receipt_date) AS receiptDate,
             c.name                                          AS customerName,
             SUM(wri.quantity)                               AS quantity
      FROM warehouse_receipt wr
               JOIN warehouse_receipt_item wri ON wr.id = wri.warehouse_receipt_id
               JOIN customer c ON c.id = wr.customer_id
          AND wr.year_id = year
      GROUP BY wr.id, receiptNo, receiptDate, customerName) wwc;
END;
$$;

alter function search_warehouse_receipts_by_receiptnumber_and_year_name(bigint, bigint) owner to postgres;
--22
create
or replace function get_all_warehouse_receipts_by_receipt_number_and_year_name(year bigint)
    returns TABLE(id bigint, description text)
    language plpgsql
as
$$
BEGIN
RETURN QUERY
SELECT wwc.id,
       CONCAT('حواله ', wwc.receiptNo, '-', wwc.customerName, ' تاریخ ', wwc.receiptDate, ' تعداد',
              wwc.quantity) AS description
FROM (SELECT wr.id,
             wr.warehouse_receipt_number                     AS receiptNo,
             gregorian_to_persian(wr.warehouse_receipt_date) AS receiptDate,
             c.name                                          AS customerName,
             SUM(wri.quantity)                               AS quantity
      FROM warehouse_receipt wr
               JOIN warehouse_receipt_item wri ON wr.id = wri.warehouse_receipt_id
               JOIN customer c ON c.id = wr.customer_id
      WHERE wr.year_id = year
      GROUP BY wr.id, receiptNo, receiptDate, customerName) wwc;
END;
$$;

alter function get_all_warehouse_receipts_by_receipt_number_and_year_name(bigint) owner to postgres;

create function get_all_contracts_by_year_name(year_name bigint)
    returns TABLE
            (
                id                   bigint,
                contract_number      character varying,
                contract_description character varying,
                startdate            date,
                enddate              date,
                customer_name        character varying,
                total_amount         numeric,
                total_quantity       numeric
            )
    language plpgsql
as
$$
BEGIN
    RETURN QUERY
        SELECT c.id,
               c.contract_number,
               c.contract_description,
               c.start_date                                  AS startdate,
               c.end_date                                    AS enddate,
               c2.name                                       AS customer_name,
               COALESCE(SUM(ci.unit_price * ci.quantity), 0) AS total_amount,
               COALESCE(SUM(ci.quantity), 0)                 AS total_quantity
        FROM contracts c
                 join year y on y.id = c.year_id
                 join customer c2 on c2.id = c.customer_id
                 join contract_item ci on c.id = ci.contract_id
        WHERE y.name = year_name
        GROUP BY c.id, c.contract_number, c.contract_description, c.start_date, c.end_date, c2.name
        ORDER BY c.id;

END;
$$;

alter function get_all_contracts_by_year_name(bigint) owner to postgres;



