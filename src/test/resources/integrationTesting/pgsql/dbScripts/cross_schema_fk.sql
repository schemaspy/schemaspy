DROP SCHEMA IF EXISTS schA CASCADE;


DROP SCHEMA IF EXISTS schB CASCADE;


CREATE SCHEMA schA AUTHORIZATION test;


CREATE SCHEMA schB AUTHORIZATION test;


CREATE TABLE schA.tableA (
    idA    integer        PRIMARY KEY,
    otherIdB     integer
);


CREATE TABLE schB.tableB (
    idB    integer        PRIMARY KEY,
    otherIdC     integer
);


ALTER TABLE schA.tableA ADD CONSTRAINT cross_fk FOREIGN KEY (otherIdB) REFERENCES schB.tableB(idB);

