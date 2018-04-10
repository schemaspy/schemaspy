create database test1;
create database test2;
create table test1.a(id1 int);
create view test2.a as select id1 as id2 from test1.a;
create user 'testUser'@'%' identified by 'password';
grant select, show view on test1.* to 'testUser'@'%';
grant select, show view on test2.* to 'testUser'@'%';
flush privileges