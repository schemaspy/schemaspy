create database test in rootdbs;



database test;



create table test(
  id int,
  firstname char(32),
  lastname char(32),
  age smallint,
  weight smallint,
  height SMALLINT
);




create index test_index on test
    (firstname,lastname,age) using btree ;




alter table test add constraint primary key (id)
    constraint pk_id  ;




CREATE FUNCTION gc_init(dummy VARCHAR(255)) RETURNING LVARCHAR;
    RETURN '';
END FUNCTION;




CREATE FUNCTION gc_iter(result LVARCHAR, value VARCHAR(255))
    RETURNING LVARCHAR;
    IF result = '' THEN
        RETURN TRIM(value);
    ELSE
        RETURN result || ',' || TRIM(value);
    END IF;
END FUNCTION;




CREATE FUNCTION gc_comb(partial1 LVARCHAR, partial2 LVARCHAR)
    RETURNING LVARCHAR;
    IF partial1 IS NULL OR partial1 = '' THEN
        RETURN partial2;
    ELIF partial2 IS NULL OR partial2 = '' THEN
        RETURN partial1;
    ELSE
        RETURN partial1 || ',' || partial2;
    END IF;
END FUNCTION;




CREATE FUNCTION gc_fini(final LVARCHAR) RETURNING LVARCHAR;
    RETURN final;
END FUNCTION;




CREATE AGGREGATE group_concat
    WITH (INIT = gc_init, ITER = gc_iter,
          COMBINE = gc_comb, FINAL = gc_fini);


