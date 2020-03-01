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

ALTER TABLE test
   ADD CONSTRAINT CHECK ((LENGTH(firstname) > 10 AND LENGTH(lastname) > 10 AND age BETWEEN 100 AND 105 AND weight BETWEEN 100 and 105 and height BETWEEN 100 and 105) OR ((LENGTH(firstname) > 13 AND LENGTH(lastname) > 13 AND age BETWEEN 106 AND 108 AND weight BETWEEN 106 and 108 and height BETWEEN 106 and 108))) CONSTRAINT big_check;