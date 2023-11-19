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
          AND wr.year_id = year
      GROUP BY wr.id, receiptNo, receiptDate, customerName) wwc;
END;
$$;

alter function get_all_warehouse_receipts_by_receipt_number_and_year_name(bigint) owner to postgres;

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























