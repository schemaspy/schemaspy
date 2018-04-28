USE master;
CREATE DATABASE CheckConstraint;
USE CheckConstraint;
CREATE SCHEMA CheckConstraint;
CREATE TABLE CheckConstraint.range (
  id int PRIMARY KEY IDENTITY(1,1) NOT NULL,
  lower_bound int NOT NULL,
  upper_bound int NOT NULL,
  CONSTRAINT CHK_bound
  CHECK (lower_bound < range.upper_bound)
);

