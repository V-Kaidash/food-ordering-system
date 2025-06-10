DROP SCHEMA IF EXISTS customer CASCADE;

CREATE SCHEMA customer;

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE customer.customers
(
    id UUID NOT NULL,
    username CHARACTER VARYING NOT NULL,
    first_name CHARACTER VARYING NOT NULL,
    last_name CHARACTER VARYING NOT NULL,
    CONSTRAINT customers_pkey PRIMARY KEY (id)
);

DROP MATERIALIZED VIEW IF EXISTS customer.order_customer_m_view;

CREATE MATERIALIZED VIEW customer.order_customer_m_view
TABLESPACE pg_default
AS
SELECT id, username, first_name, last_name FROM customer.customers
    WITH DATA;

REFRESH MATERIALIZED VIEW customer.order_customer_m_view;

DROP FUNCTION IF EXISTS customer.refresh_order_customer_m_view;

CREATE OR REPLACE FUNCTION customer.refresh_order_customer_m_view()
RETURNS TRIGGER
AS '
    BEGIN
        REFRESH MATERIALIZED VIEW customer.order_customer_m_view;
        RETURN NULL;
    END;
' LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS refresh_order_customer_m_view ON customer.customers;

CREATE TRIGGER refresh_order_customer_m_view
    AFTER INSERT OR UPDATE OR DELETE OR TRUNCATE
                    ON customer.customers
                        FOR EACH STATEMENT
                        EXECUTE FUNCTION customer.refresh_order_customer_m_view();
