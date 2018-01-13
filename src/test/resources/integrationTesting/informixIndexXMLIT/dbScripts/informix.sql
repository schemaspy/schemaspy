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