DROP SCHEMA IF EXISTS polyfunc CASCADE;


CREATE SCHEMA polyfunc AUTHORIZATION test;


CREATE TABLE polyfunc.testdata (
                                   id bigint NOT NULL,
                                   value text
);


ALTER TABLE ONLY polyfunc.testdata ADD CONSTRAINT testdata_pkey PRIMARY KEY (id);


CREATE FUNCTION polyfunc.bar(_id bigint) RETURNS bigint
    LANGUAGE sql
    AS $$
    SELECT COUNT(*) FROM polyfunc.testdata WHERE id = $1
$$;


ALTER FUNCTION polyfunc.bar(_id bigint) OWNER TO test;


COMMENT ON FUNCTION polyfunc.bar(_id bigint) IS 'This is the comment containing <value> or <item>';


CREATE FUNCTION polyfunc.foo(_val text) RETURNS bigint
    LANGUAGE sql
    AS $$
SELECT COUNT(*) FROM polyfunc.testdata WHERE value = $1
$$;


ALTER FUNCTION polyfunc.foo(_val text) OWNER TO test;


CREATE FUNCTION polyfunc.foo(_id bigint, _val text) RETURNS bigint
    LANGUAGE sql
    AS $$
SELECT COUNT(*) FROM polyfunc.testdata WHERE id = $1 AND value = $2
$$;


ALTER FUNCTION polyfunc.foo(_id bigint, _val text) OWNER TO test;