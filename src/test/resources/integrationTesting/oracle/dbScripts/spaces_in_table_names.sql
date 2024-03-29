CREATE TABLESPACE oraspaceit DATAFILE '/u01/app/oracle/oradata/XE/orasit.dbf' SIZE 50M EXTENT MANAGEMENT LOCAL AUTOALLOCATE;
CREATE TEMPORARY TABLESPACE oraspaceittmp TEMPFILE '/u01/app/oracle/oradata/XE/orasittmp.dbf' SIZE 20M REUSE EXTENT MANAGEMENT LOCAL UNIFORM SIZE 16M;
CREATE USER oraspaceit IDENTIFIED BY oraspaceit123 DEFAULT TABLESPACE oraspaceit QUOTA 100M ON oraspaceit TEMPORARY TABLESPACE oraspaceittmp;
GRANT CREATE SESSION TO oraspaceit;
GRANT CREATE TABLE TO oraspaceit;
CREATE TABLE oraspaceit."test 1.0" (id NUMBER(3,0) PRIMARY KEY, name VARCHAR2(15));