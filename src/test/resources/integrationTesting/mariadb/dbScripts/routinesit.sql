CREATE TABLE issue222 (
  rid INTEGER NOT NULL
) engine=InnoDB COMMENT 'Only to avoid issue 222';

CREATE FUNCTION no_det (s CHAR(20))
RETURNS CHAR(50) NO SQL NOT DETERMINISTIC
RETURN CONCAT('Hello, ',s,'!');

CREATE FUNCTION yes_det (s CHAR(20))
RETURNS CHAR(50) NO SQL DETERMINISTIC
RETURN CONCAT('Hello, ',s,'!');