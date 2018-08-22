DROP SCHEMA IF EXISTS mview CASCADE;


CREATE SCHEMA mview AUTHORIZATION postgres;


CREATE TABLE mview.invoice (
    invoice_no    integer        PRIMARY KEY,
    seller_no     integer,       -- ID of salesperson
    invoice_date  date,          -- date of sale
    invoice_amt   numeric(13,2)  -- amount of sale
);


CREATE MATERIALIZED VIEW mview.sales_summary AS
  SELECT
      seller_no,
      invoice_date,
      sum(invoice_amt)::numeric(13,2) as sales_amt
    FROM mview.invoice
    WHERE invoice_date < CURRENT_DATE
    GROUP BY
      seller_no,
      invoice_date
    ORDER BY
      seller_no,
      invoice_date;


CREATE UNIQUE INDEX sales_summary_seller
  ON mview.sales_summary (seller_no, invoice_date);