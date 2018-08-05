create database schemaleak;
create database schemaleaktest;
create table schemaleak.a(id1 int);
create view schemaleaktest.a as select id1 as id2 from schemaleak.a;
create user 'testUser'@'%' identified by 'password';
grant select, show view on schemaleak.* to 'testUser'@'%';
grant select, show view on schemaleaktest.* to 'testUser'@'%';
flush privileges